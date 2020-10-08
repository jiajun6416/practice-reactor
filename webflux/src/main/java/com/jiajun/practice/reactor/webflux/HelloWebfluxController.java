package com.jiajun.practice.reactor.webflux;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.ImmutableMap;
import com.ximalaya.common.auth.common.subject.WebfluxUserContext;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @author jiajun
 */
@RestController
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

    @RequestMapping("exclude/{ts}")
    public Mono<Object> exclude(ServerWebExchange exchange) {
        List<? extends Serializable> userInfo = Arrays.asList(
                WebfluxUserContext.getCurrentUid(exchange),
                WebfluxUserContext.getCurrentSessionId(exchange)
        );
        return WebfluxUserContext.get().map(session -> JSON.toJSONString(session.orElse(null)) + "&" + JSON.toJSONString(userInfo));
    }

    @RequestMapping("free/{ts}")
    public Mono<Object> free(ServerWebExchange exchange,
                             @RequestAttribute(value = WebfluxUserContext.USER_ID) Optional<Long> uid
    ) {

        List<? extends Serializable> userInfo = Arrays.asList(
                WebfluxUserContext.getCurrentUid(exchange),
                WebfluxUserContext.getCurrentSessionId(exchange)
        );
        return WebfluxUserContext.get().map(session -> JSON.toJSONString(session.orElse(null)) + "&" + JSON.toJSONString(userInfo));
    }

    @RequestMapping("include/{ts}")
    public Mono<Object> include(ServerWebExchange exchange,
                                @RequestAttribute(value = WebfluxUserContext.USER_ID) Optional<Long> uid
    ) {
        List<? extends Serializable> userInfo = Arrays.asList(
                WebfluxUserContext.getCurrentUid(exchange),
                WebfluxUserContext.getCurrentSessionId(exchange)
        );
        return WebfluxUserContext.get().map(session -> JSON.toJSONString(session.orElse(null)) + "&" + JSON.toJSONString(userInfo));
    }

    @RequestMapping("free/date/{ts}")
    public Mono<Object> date(ServerWebExchange exchange,
                             @RequestAttribute(value = WebfluxUserContext.USER_ID) Optional<Long> uid,
                             @RequestParam Long albumId
    ) {
        int i = 1 / 0;
        return Mono.just(ImmutableMap.of("date", new Date()));
    }
}
