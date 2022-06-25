package com.jiajun.reactor.projectreactor;

import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * 回压: Reactor中最为重要的点, Push模式下的服务保护机制
 * 指定回压策略:
 * - {@link Flux#onBackpressureDrop()} : 在subscribe处理不了时丢弃该消息
 * - {@link Flux#onBackpressureError()}: 丢异常
 * - {@link Flux#onBackpressureLatest()}: 只push最新的消息
 * - {@link Flux#onBackpressureBuffer()}: 将未被subscribe处理的消息缓存起来, 默认是无界队列. (默认策略)
 *
 * @author jiajun
 */
public class BackPressure {

    @Test
    public void onBackpressureDrop() {
        Flux.interval(Duration.ofMillis(1))
                .onBackpressureDrop()
                .concatMap(a -> Mono.delay(Duration.ofMillis(10)).thenReturn(a))
                .doOnNext(a -> System.out.println("Element kept by consumer: " + a))
                .blockLast();
    }
}
