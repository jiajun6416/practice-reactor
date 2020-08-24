package com.jiajun.practice.reactor.webflux;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.EncoderHttpMessageWriter;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.result.method.annotation.ResponseBodyResultHandler;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;

/**
 * @author jiajun
 */
@SpringBootApplication
public class Application implements WebFluxConfigurer {

    public static void main(String[] args) {
        new SpringApplication(Application.class).run(args);
    }

    @Resource
    private ResponseBodyResultHandler responseBodyResultHandler;

    @Bean
    public WebFilter customFilter() {
        EncoderHttpMessageWriter json = new EncoderHttpMessageWriter(new Jackson2JsonEncoder());
        return new WebFilter() {

            @Override
            public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
                ServerHttpRequest request = exchange.getRequest();
                ServerHttpResponse response = exchange.getResponse();
                // 写回json
                response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                Mono<Void> result = response.writeWith(
                        Mono.just(response.bufferFactory().wrap("{\"ret\":50,\"msg\":\"请登录\"}".getBytes()))
                );

                // 重定向
                //response.setStatusCode(HttpStatus.FOUND);
                //response.getHeaders().setLocation(URI.create("https://www.ximalaya.com"));
                result = Mono.empty();

                result = chain.filter(exchange).subscriberContext(ctx -> ctx.put("uid", 1));
                return result.then();
            }
        };
    }
}
