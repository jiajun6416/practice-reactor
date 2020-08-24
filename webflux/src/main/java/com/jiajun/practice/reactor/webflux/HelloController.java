package com.jiajun.practice.reactor.webflux;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * @author jiajun
 */
@RestController
@RequestMapping("hello")
public class HelloController {

    @GetMapping("get/{ts}")
    public Object simpleGet(@PathVariable String ts) {
        return "hello webflux: " + ts;
    }

    @GetMapping("get2/{ts}")
    public Mono<Object> simpleGet2(@PathVariable String ts) {
        return Mono.subscriberContext().map(ctx -> ctx.getOrEmpty("uid")).map(
                uid -> "hello webflux: " + ts + ", uid: " + uid
        );
    }
}
