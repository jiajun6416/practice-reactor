package com.jiajun.reactor.circuit;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * 基于Project Reactor简单实现的滑动窗口, 参考mainstay中的熔断和恢复机制:
 *
 *
 * {@link CircuitBreaker} 代表一个资源的熔断器, manager通过一个滑动窗口管理多个熔断器
 *
 * @see com.ximalaya.mainstay.extension.circuit.CircuitBreaker
 * - 低版本: 类似Hystrix中的降级, 基于RxJava, 创建对象比较多, 带来GC问题
 * - 优化后: 参考蚂蚁的`sofa-lookout`, 性能较好, 大量使用了对象复用, 较少的GC
 */
public class CircuitBreakerManager {

    private Map<String, CircuitBreaker> circuitBreakers = new ConcurrentHashMap<>();

    public CircuitBreakerManager() {
        // 启动滑动窗口, Reactor中滑动窗口默认会有一个单独的线程
        metricCalc();
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
     * 对应资源是否可以请求
     * @param id
     * @return
     */
    public Boolean allowRequest(String id) {
        return Optional.ofNullable(circuitBreakers.get(id)).map(CircuitBreaker::allowRequest).orElse(true);
    }

    private Sinks.Many<InvokeResult> sink = Sinks.unsafe().many().unicast().onBackpressureBuffer(); // unsafe: 支持多线程emit. unicast: 单播

    /**
     * 滑动窗口统计数据.
     * 数据发射使用的是一个单独的线程 {@link Flux#window(java.time.Duration)}
     */
    public void metricCalc() {
        Flux<WindowBucket> bucketFlux = sink.asFlux().window(Duration.ofSeconds(1))
                // 按照资源id进行分组
                .flatMap(flux -> flux.groupBy(InvokeResult::getId, Function.identity()), 8, 256)
                .flatMap(sourceInvokeResult -> {
                    // 聚合一个bucket中的数据
                    String id = sourceInvokeResult.key();
                    // scan: 类似于 java.util.stream.Collectors.reducing(T, java.util.function.BinaryOperator<T>)
                    return sourceInvokeResult.scan(new WindowBucket(id), (bucket, invokeResult) -> {
                        bucket.requestCnt++;
                        if (!invokeResult.isSuccess()) {
                            bucket.errorCnt++;
                        }
                        return bucket;
                    });
                    // .last(new WindowBucket(id)); // 如果没有元素, 返回一个默认值
                }, 8, 256);

        // 窗口数据给对应断路器
        bucketFlux.subscribe(windowBucket -> {
            String id = windowBucket.getId();
            // 实际有请求时创建熔断器加入
            CircuitBreaker circuitBreaker = Optional.ofNullable(circuitBreakers.get(id)).orElseGet(() -> circuitBreakers.computeIfAbsent(id, unused -> new CircuitBreaker(id)));
            circuitBreaker.updateNewBucket(windowBucket);
        });
    }
}
