package com.jiajun.reactor.projectreactor;

import com.google.common.util.concurrent.Uninterruptibles;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.util.concurrent.Queues;

import java.time.Duration;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

public class SinksSpec {

    /**
     * Cold
     * 1. 序列发射延迟特性, 在订阅前什么都不会发生
     * 2. 每个订阅都会导致数据流重新发一遍!!
     */
    @Test
    public void cold() {
        Flux<Integer> flux = Flux.generate(() -> 0, (integer, sink) -> {
            System.out.println("do publish: " + integer);
            if (integer > 10) {
                sink.complete();
            }
            integer++;
            sink.next(integer);
            return integer;
        });
        flux.subscribe(i -> System.out.println("subscribe1: " + i));
        Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
        flux.subscribe(i -> System.out.println("subscribe2: " + i)); // 第二次订阅同样会获取到所有的序列
    }

    /**
     * Hot
     * 1. 序列是立即发射, 即使没有订阅者.  例如Flux#just. 但通过热序列还是使用Processor生成的
     * 2. 订阅者只能获取到订阅之后产生的数据
     */

    /**
     * project-reactor 3.5后删除
     */
    @Test
    public void directProcessor() {
/*        FluxIdentityProcessor<Object> unicastProcessor = Processors.unicast();
        Sinks.StandaloneFluxSink<Object> unicast = Sinks.unicast();

        // UnicastProcessor: 热发布者
        UnicastProcessor<String> hotSequence = UnicastProcessor.create();
        Flux<String> hotFlux = hotSequence.publish().autoConnect();

        hotFlux.subscribe(d -> System.out.println("Subscriber 1 to Hot Source: "+d));

        hotSequence.onNext("blue");
        hotSequence.onNext("green");

        Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
        hotFlux.subscribe(d -> System.out.println("Subscriber 2 to Hot Source: "+d));

        hotSequence.onNext("orange");
        hotSequence.onNext("purple");
        hotSequence.onComplete();*/
    }

    /**
     * unicast: 单一传播, 即只支持一个订阅者
     * <p>
     * 只能有一个订阅者, 支持设置发送的回压处理策略
     * 如果没有订阅者，那么保存接收的消息直到第一个订阅者订阅
     */
    @Test
    public void unicast() {
        // case1: 有界队列, 无消费者
        Queue<Long> buffer1 = Queues.<Long>get(16).get();
        Sinks.Many<Long> sink1 = Sinks.many().unicast().onBackpressureBuffer(buffer1);
        Flux.interval(Duration.ofMillis(10)).subscribe(aLong -> {
            sink1.emitNext(aLong, (signalType, emitResult) -> {
                //System.out.println("signalType: " + signalType + ", emitResult" + emitResult + ", bufferSize: " + buffer1.size());
                return false; // 不重发
            });
        });

        // case2: 只支持一个订阅者
        Sinks.Many<String> sink2 = Sinks.many().unicast().onBackpressureBuffer(Queues.<String>get(2000).get()); // Sinks.many类型的sink
        Flux<String> fluxView2 = sink2.asFlux(); // 可以转为Flux
        fluxView2.subscribe(item -> System.out.println("item: " + item));
        // fluxView2.subscribe(item -> System.out.println("item: " + item)); // 会丢异常, 只支持一个订阅者

        Flux.interval(Duration.ofMillis(1000)).subscribe(aLong -> {
            sink2.emitNext("emitNext1: " + Thread.currentThread().getName() + ": " + aLong, Sinks.EmitFailureHandler.FAIL_FAST);
            sink2.emitNext("emitNext2: " + Thread.currentThread().getName() + ": " + aLong, Sinks.EmitFailureHandler.FAIL_FAST);
        });

        // 不支持在多个线程中同时调用next, 会有线程安全问题, 回报非串行化异常
        Flux.interval(Duration.ofMillis(1000)).subscribe(aLong -> {
            //sink2.tryEmitNext("emitNext3: " + Thread.currentThread().getName() + ": " + aLong);
        });

        Uninterruptibles.sleepUninterruptibly(3, TimeUnit.SECONDS);
        sink2.tryEmitComplete(); // 完成
        System.out.println("sink tryEmitComplete!");
    }

    /**
     * multicast: 广播, 支持多个订阅者
     * <p>
     * 不支持多线程调用{@link Sinks.Many#emitNext}
     * <p>
     * 支持多个订阅者, 支持设置发送的回压处理策略, 所有的消息
     */
    @Test
    public void multicast() {
        Sinks.MulticastSpec multicast = Sinks.many().multicast();
        // 缓冲指定数量, 假设所有订阅者都断开了则自动清空缓存
        Sinks.Many<Integer> sinks = multicast.onBackpressureBuffer(2000, true);
        // multicast.directAllOrNothing();// 有一个消费者消费不拉消息导致阻塞, 则消息直接丢弃
        // multicast.directBestEffort(); // 不拉消息的消费者丢弃消息, 不影响其他消费者

        // 支持多个订阅者
        Flux<Integer> flux = sinks.asFlux();
        flux.subscribe(item -> System.out.println("subscribe1 item: " + item));
        flux.subscribe(item -> System.out.println("subscribe2 item: " + item));

        new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                sinks.emitNext(i, (signalType, emitResult) -> false);
            }
        }).start();
        Uninterruptibles.sleepUninterruptibly(3, TimeUnit.SECONDS);
    }

    /**
     * 缓存指定数量的消息, 后续订阅者可以消费到
     */
    @Test
    public void replay() {
        Sinks.many().replay().limit(10000);
        Sinks.many().replay().limit(Duration.ofMillis(1));
        Sinks.many().replay().all(); //
    }

    /**
     * unsafe操作符: 支持多线程并发调用sink#next, 其序列顺序是乱序的(非线程安全, 非有序)
     */
    @Test
    public void sinksUnsafe() {
        Sinks.Many<String> unsafeSink = Sinks.unsafe().many().unicast().onBackpressureBuffer(Queues.<String>get(2000).get());
        Flux<String> fluxView = unsafeSink.asFlux();
        fluxView.subscribe(item -> System.out.println("item: " + item));

        Flux.interval(Duration.ofMillis(1000)).subscribe(aLong -> {
            unsafeSink.emitNext("emitNext1: " + Thread.currentThread().getName() + ": " + aLong, Sinks.EmitFailureHandler.FAIL_FAST);
            unsafeSink.emitNext("emitNext2: " + Thread.currentThread().getName() + ": " + aLong, Sinks.EmitFailureHandler.FAIL_FAST);
        });

        // 不支持在多个线程中同时调用next, 会有线程安全问题
        Flux.interval(Duration.ofMillis(1000)).subscribe(aLong -> {
            unsafeSink.tryEmitNext("emitNext3: " + Thread.currentThread().getName() + ": " + aLong);
        });

        Uninterruptibles.sleepUninterruptibly(3, TimeUnit.SECONDS);
        unsafeSink.tryEmitComplete(); // 完成
    }

    /**
     * hot流, 消费者只能收到订阅后的数据
     */
    @Test
    public void hot() {
        // directBestEffort: 只会丢弃慢消费者的消息, 其他消费者不受影响
        Sinks.Many<String> hotSource = Sinks.unsafe().many().multicast().directBestEffort();

        Flux<String> hotFlux = hotSource.asFlux().map(String::toUpperCase);
        hotSource.emitNext("noSubscribe", Sinks.EmitFailureHandler.FAIL_FAST);

        hotFlux.subscribe(d -> System.out.println("Subscriber 1 to Hot Source: " + d));

        hotSource.emitNext("blue", Sinks.EmitFailureHandler.FAIL_FAST);
        hotSource.tryEmitNext("green").orThrow();

        hotFlux.subscribe(d -> System.out.println("Subscriber 2 to Hot Source: " + d));

        hotSource.emitNext("orange", Sinks.EmitFailureHandler.FAIL_FAST);
        hotSource.emitNext("purple", Sinks.EmitFailureHandler.FAIL_FAST);
        hotSource.emitComplete(Sinks.EmitFailureHandler.FAIL_FAST);
    }
}
