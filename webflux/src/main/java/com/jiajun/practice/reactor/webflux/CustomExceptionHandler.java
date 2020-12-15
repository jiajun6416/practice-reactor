package com.jiajun.practice.reactor.webflux;

import com.google.common.collect.ImmutableMap;
import com.ximalaya.common.auth.common.subject.WebfluxUserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

import java.util.Optional;

/**
 * 同样可以通过 @ExceptionHandler实现, 和Mvc中区别: @ControllerAdvice只会处理进入Handler的请求
 *
 * @see ExceptionHandler
 */
@Slf4j
@ControllerAdvice
public class CustomExceptionHandler {

    @ResponseBody
    @ExceptionHandler(Throwable.class)
    public Object handle(ServerWebExchange exchange, Throwable ex) {
        if (ResponseStatusException.class.isInstance(ex)) {
            log.warn(ex.getMessage());
        } else {
            log.error(ex.getMessage(), ex);
        }
        Long uid = Optional.ofNullable(WebfluxUserContext.getCurrentLongUid(exchange)).orElse(0L);
        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        return ImmutableMap.of(
                "ret", -1,
                "uid", uid,
                "msg", ex.getMessage()
        );
    }
}
