package com.jiajun.reactor.projectreactor;

import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义动态源: 将Flux的Sink暴露出来
 * @see https://www.infoq.cn/article/reactor-by-example
 *
 * @author jiajun
 */
public class DynamicFeed {

    /**
     * 发布
     *
     * @param <T>
     */
    static class EventMulticaster<T> {

        private List<EventListener<T>> listeners = new ArrayList<>();

        public void register(EventListener<T> eventListener) {
            listeners.add(eventListener);
        }

        public void multicastEvent(T event) {
            listeners.forEach(it -> it.onEvent(event));
        }
    }

    /**
     * 订阅
     *
     * @param <T>
     */
    interface EventListener<T> {
        void onEvent(T event);
    }

    public static void main(String[] args) {
        EventMulticaster<String> eventPublisher = new EventMulticaster<>();

        Flux<String> flux = Flux.create(fluxSink -> {
                    EventListener<String> eventListener = fluxSink::next;
                    eventPublisher.register(eventListener);
                },
                FluxSink.OverflowStrategy.BUFFER // 缓存策略
        );
        ConnectableFlux<String> dynamicPublisher = flux.publish();

        dynamicPublisher.subscribe(System.out::println);

        dynamicPublisher.connect();


    }
}
