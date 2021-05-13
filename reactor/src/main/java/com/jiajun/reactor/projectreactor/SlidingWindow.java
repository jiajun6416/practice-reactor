package com.jiajun.reactor.projectreactor;

import com.alibaba.fastjson.JSON;
import com.google.common.util.concurrent.Uninterruptibles;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * ProjectReactor实现滑动窗口
 * 1. hot sinks
 * 2. window api
 *
 * @author jiajun
 */
public class SlidingWindow {

    private Sinks.Many<InvokeResult> sink = Sinks
            .unsafe()
            .many().multicast().directBestEffort();

    private Flux<InvokeResult> fluxView = sink.asFlux();

    @Data
    @AllArgsConstructor
    static class InvokeResult {
        private String method; // 方法

        private boolean success; // 请求结果

        private int costTime; // 耗时
    }

    @Data
    @AllArgsConstructor
    static class InvokeSecondResult {
        private String method; // 方法

        private int successNum; // 一秒内成功总数

        private int failNum; // 一秒内失败总数

        private int aveCostTime; // 平均耗时

        private int totalCostTime; // 总耗时

        public InvokeSecondResult(String method) {
            this.method = method;
        }
    }

    @Test
    public void slidingWindowTest() {
        Flux.interval(Duration.ofMillis(1)).subscribe(unused -> {
            InvokeResult result = new InvokeResult("method1", ThreadLocalRandom.current().nextInt(10) > 3, ThreadLocalRandom.current().nextInt(100));
            sink.emitNext(result, Sinks.EmitFailureHandler.FAIL_FAST);
        });
        Flux.interval(Duration.ofMillis(1)).subscribe(unused -> {
            InvokeResult result = new InvokeResult("method2", ThreadLocalRandom.current().nextInt(10) > 4, ThreadLocalRandom.current().nextInt(100));
            sink.emitNext(result, Sinks.EmitFailureHandler.FAIL_FAST);
        });
        Flux.interval(Duration.ofMillis(1)).subscribe(unused -> {
            InvokeResult result = new InvokeResult("method3", ThreadLocalRandom.current().nextInt(10) > 5, ThreadLocalRandom.current().nextInt(100));
            sink.emitNext(result, Sinks.EmitFailureHandler.FAIL_FAST);
        });

        // window处理: 1. group 2. 计算窗口
        Flux<InvokeSecondResult> invokeSecondResultFlux = fluxView.window(Duration.ofSeconds(1))
                .flatMap(flux -> flux.groupBy(InvokeResult::getMethod, Function.identity()), 8, 256) // flatmap 最大的一批数量, 过大，内部 group by 也会慢
                .flatMap(groupByMethodResultFlux -> {
                    String methodName = groupByMethodResultFlux.key();
                    // 有初始值的迭代
                    return groupByMethodResultFlux.scan(new InvokeSecondResult(methodName), (invokeSecondResult, invokeResult) -> {
                        if (invokeResult.isSuccess()) {
                            invokeSecondResult.setSuccessNum(invokeSecondResult.getSuccessNum() + 1);
                        } else {
                            invokeSecondResult.setFailNum(invokeSecondResult.getFailNum() + 1);
                        }
                        invokeSecondResult.setTotalCostTime(invokeSecondResult.getTotalCostTime() + invokeResult.getCostTime());
                        return invokeSecondResult;
                    }).last(new InvokeSecondResult(methodName)); // 如果没有元素, 返回一个默认值
                }, 8, 256);
        // window结果订阅
        invokeSecondResultFlux.subscribe(secondResult -> {
            if (secondResult.getTotalCostTime() > 0) {
                secondResult.setAveCostTime(secondResult.getTotalCostTime() / (secondResult.getSuccessNum() + secondResult.getFailNum()));
            }
            System.out.println("ts: " + System.currentTimeMillis() + ": " + JSON.toJSONString(secondResult));
        });
        fluxView.window(Duration.ofSeconds(1)).flatMap(Flux::collectList).subscribe(invokeResults -> System.out.println("results size: " + invokeResults.size()));

        Uninterruptibles.sleepUninterruptibly(10, TimeUnit.SECONDS);
    }
}
