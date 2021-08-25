package com.jiajun.reactor.circuit;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

/**
 * 基于Project Reactor简单实现的滑动窗口, 参考mainstay中的熔断和恢复机制:
 *
 * @author jiajun
 * @see com.ximalaya.mainstay.extension.circuit.CircuitBreaker
 * - 低版本: 类似Hystrix中的降级, 基于RxJava, 创建对象比较多, 带来GC问题
 * - 优化后: 参考蚂蚁的`sofa-lookout`, 性能较好, 大量使用了对象复用, 较少的GC
 */
public class CircuitBreakerManager {

    private static Sinks.Many<InvokeResult> sink = Sinks.unsafe().many().multicast().directBestEffort();

    private static Flux<InvokeResult> fluxView = sink.asFlux();

    private static CopyOnWriteArrayList<CircuitBreaker> circuitBreakers = new CopyOnWriteArrayList<>();

    public CircuitBreakerManager() {
        // 控制单例
        metricCalc();
    }

    /**
     * 注册熔断器
     *
     * @param circuitBreaker
     */
    public void register(CircuitBreaker circuitBreaker) {
        circuitBreakers.add(circuitBreaker);
    }

    /**
     * 单次调用结果
     *
     * @param invokeResult
     */
    public void addResult(InvokeResult invokeResult) {
        sink.emitNext(invokeResult, Sinks.EmitFailureHandler.FAIL_FAST);
    }

    /**
     * 滑动窗口统计数据
     */
    public void metricCalc() {
        Flux<WindowBucket> invokeSecondResultFlux = fluxView.window(Duration.ofSeconds(1))
                // flatmap 最大的一批数量, 过大，内部 group by 也会慢
                .flatMap(flux -> flux.groupBy(InvokeResult::getId, Function.identity()), 8, 256)
                .flatMap(groupByMethodResultFlux -> {
                    String id = groupByMethodResultFlux.key();
                    // 有初始值的迭代
                    return groupByMethodResultFlux.scan(new WindowBucket(id), (invokeSecondResult, invokeResult) -> {
                        invokeSecondResult.setRequestCnt(invokeSecondResult.getRequestCnt() + 1);
                        if (!invokeResult.isSuccess()) {
                            invokeSecondResult.setErrorCnt(invokeSecondResult.getErrorCnt() + 1);
                        }
                        return invokeSecondResult;
                    }).last(new WindowBucket(id)); // 如果没有元素, 返回一个默认值
                }, 8, 256);

        // 窗口数据给断路器
        invokeSecondResultFlux.subscribe(windowBucket -> circuitBreakers.forEach(circuitBreaker -> circuitBreaker.updateRollingWindowInfo(windowBucket)));
    }
}
