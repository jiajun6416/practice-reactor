package com.jiajun.reactor.circuit;

import com.google.common.util.concurrent.Uninterruptibles;
import org.junit.Test;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class CircuitBreakerManagerTest {

    /**
     * {@link CircuitBreakerManager} : 支持数个资源的熔断管理器
     */
    @Test
    public void circuitBreaker() {
        CircuitBreakerManager circuitBreakerManager = new CircuitBreakerManager();
        Flux.interval(Duration.ofMillis(500)).subscribe(ts -> {
            String api = "trackInfo";
            if (circuitBreakerManager.allowRequest(api)) {
                InvokeResult invokeResult = new InvokeResult(api, ThreadLocalRandom.current().nextInt(10) > 6, ThreadLocalRandom.current().nextInt(100));
                circuitBreakerManager.addResult(invokeResult);
            }
        });
        Flux.interval(Duration.ofMillis(500)).subscribe(ts -> {
            String api = "albumInfo";
            if (circuitBreakerManager.allowRequest(api)) {
                InvokeResult invokeResult = new InvokeResult(api, ThreadLocalRandom.current().nextInt(10) > 3, ThreadLocalRandom.current().nextInt(100));
                circuitBreakerManager.addResult(invokeResult);
            }
        });

        Uninterruptibles.sleepUninterruptibly(1, TimeUnit.MINUTES);
    }
}