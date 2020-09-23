package com.jiajun.practice.reactor.webflux;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * @author jiajun
 */
@RestController
@RequestMapping("webflux")
public class HelloWebfluxController {

    @GetMapping("get/{ts}")
    public Mono<Object> simpleGet(@PathVariable String ts) {
        return Mono.just(Optional.of("hello webflux: " + ts));
    }

    @GetMapping("get2/{ts}")
    public Mono<Object> simpleGet2(@PathVariable String ts) {
        return Mono.subscriberContext().map(ctx -> ctx.getOrEmpty("uid")).map(
                uid -> "hello webflux: " + ts + ", uid: " + uid
        );
    }
}
