package com.jiajun.webclient;

import com.alibaba.fastjson.JSON;
import com.google.common.util.concurrent.Uninterruptibles;
import org.junit.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.concurrent.TimeUnit;

/**
 * @author jiajun
 */
public class WebClientTest {

    ParameterizedTypeReference<ServerSentEvent<String>> type = new ParameterizedTypeReference<ServerSentEvent<String>>() {
    };

    @Test
    public void sseClient() {
        WebClient webClient = WebClient.create("http://localhost:2678/chitchat-asr-business");
        Flux<ServerSentEvent<String>> sentEventFlux = webClient.get()
                .uri("/stream/events?roomId=" + 1)
                .retrieve()
                .bodyToFlux(type);

        sentEventFlux.subscribe(event -> System.out.println(JSON.toJSONString(event.toString())));
        Uninterruptibles.sleepUninterruptibly(1, TimeUnit.HOURS);
    }
}
