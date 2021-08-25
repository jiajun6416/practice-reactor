package com.jiajun.reactor.projectreactor;

import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * 回压: Reactor中最为重要的点, Push模式下的服务保护机制
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
