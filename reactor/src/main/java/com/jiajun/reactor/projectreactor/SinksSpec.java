package com.jiajun.reactor.projectreactor;

import com.google.common.util.concurrent.Uninterruptibles;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.util.concurrent.Queues;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Cold: 没有订阅者则永远不会生成序列, 无论订阅者在何时订阅数据流, 总能收到数据流中产生的全部数据
 * Hot: 没有订阅者仍然会生成序列, 订阅者只能获取到在其订阅之后产生的数据
 *
 * @author jiajun
 * https://projectreactor.io/docs/core/release/reference/index.html#reactor.hotCold
 */
public class SinksSpec {

    /**
     * Cold
     * 1. 序列发射延迟特性, 在订阅前什么都不会发生
     * 2. 每个订阅都会导致数据流重新发一遍
     */
    @Test
    public void cold() {
        Flux<Integer> flux = Flux.range(1, 10);
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
     * 只能有一个订阅者, 支持设置发送的回压处理策略
     * 如果没有订阅者，那么保存接收的消息直到第一个订阅者订阅
     */
    @Test
    public void unicast() {
        Sinks.UnicastSpec unicast = Sinks.many().unicast();
        Sinks.Many<Long> sink = unicast.onBackpressureBuffer(Queues.<Long>get(2000).get()); // Sinks.many类型的sink
        Flux<Long> fluxView = sink.asFlux(); // 可以转为Flux

        Flux.interval(Duration.ofMillis(500)).subscribe(aLong -> {
            sink.emitNext(aLong, Sinks.EmitFailureHandler.FAIL_FAST); // 线程一
            System.out.println("thread: " + Thread.currentThread().getName() + ", emitNext item: " + aLong);
        });
        Flux.interval(Duration.ofMillis(500)).subscribe(aLong -> {
            Sinks.EmitResult emitResult = sink.tryEmitNext(aLong);// 线程二
            System.out.println("thread: " + Thread.currentThread().getName() + ", tryEmitNext item: " + aLong + ", result: " + emitResult);
        });

        Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
        fluxView.subscribe(aLong -> System.out.println("subscribe1: " + System.currentTimeMillis() + ": " + aLong));
        // emitter.asFlux().subscribe(aLong -> System.out.println("subscribe2: " + aLong)); // 会丢异常, 只支持一个订阅者

        Uninterruptibles.sleepUninterruptibly(3, TimeUnit.SECONDS);
        sink.tryEmitComplete(); // 完成
        System.out.println("sink tryEmitComplete!");
    }

    /**
     * 支持多个订阅者, 支持设置发送的回压处理策略
     * 如果没有订阅者，那么接收的消息直接丢弃, 如果所有订阅者都取消, 则发射缓冲区数据(假设有)也会被清除
     */
    @Test
    public void multicast() {
        Sinks.MulticastSpec multicast = Sinks.many().multicast();
        // 缓冲指定数量, 假设所有订阅者都断开了则自动清空缓存
        Sinks.Many<Long> sinks = multicast.onBackpressureBuffer(2000, true);
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
}
