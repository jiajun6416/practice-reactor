package com.jiajun.reactor.projectreactor;

import com.alibaba.fastjson.JSON;
import com.google.common.util.concurrent.Uninterruptibles;
import com.jiajun.reactor.observer.Observer;
import org.apache.commons.lang3.event.EventListenerSupport;
import org.junit.Test;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.scheduler.Schedulers;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 使用编程方式创建序列
 * 1. generate
 * 2. create
 * 3. push
 * 4. A hybrid push/pull model
 * 5. Handle
 *
 * @author jiajun
 */
public class ProgrammaticallyCreateASequence {

    /**
     * 同步订阅模型, 逐个产生元素
     * 有点像迭代器模型. 将iterator转成stream
     * <p>
     * sink: sink指的是数据的去处(`即发给消费者的过程. 可以是存储, 计算, socket等等`)
     * - 需要一个状态值用于下一次调用
     * - 每次最多一个onNext和complete/error
     */
    @Test
    public void generate() {
        Flux<String> flux = Flux.generate(
                () -> 0,
                (status, synchronousSink) -> {
                    synchronousSink.next("status" + status);
                    if (status == 10) {
                        synchronousSink.complete();
                    }
                    return status + 1;
                },
                System.out::println // complete时回调status
        );

        flux.subscribe(System.out::println);

        Flux<List<Integer>> flux2 = Flux.generate(
                () -> Arrays.asList(3, 4),
                (lists, synchronousSink) -> {
                    if (lists.isEmpty()) {
                        synchronousSink.complete();
                    }
                    synchronousSink.next(lists);
                    Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
                    return Arrays.asList(1, 2);
                }
        );
        flux2.subscribe(integers -> System.out.println(JSON.toJSONString(integers)));
    }

    /**
     * create: 典型的push模型
     * 1. 支持异步, 可以一次产生多个元素
     * 2. 将回调模式转成响应式
     * 3. 支持控制回压
     */
    @Test
    public void create() {
        EventListenerSupport<Observer.EventListener> eventListenerSupport = EventListenerSupport.create(Observer.EventListener.class);
        Observer.EventListener listener = eventListenerSupport.fire();
        Flux<String> flux = Flux.create(fluxSink ->
                        eventListenerSupport.addListener(event -> {
                            String threadName = Thread.currentThread().getName();
                            // 每轮支持发射多个, 并且可以是不同的线程
                            new Thread(() -> fluxSink.next(Thread.currentThread().getName() + "&" + threadName + "&" + event)).start();
                            new Thread(() -> fluxSink.next(Thread.currentThread().getName() + "&" + threadName + "&" + event)).start();
                            fluxSink.onRequest(num -> System.out.println("consumer nums: " + num)); // onRequest回调
                        })
                ,
                FluxSink.OverflowStrategy.BUFFER // 回压策略, 默认是缓存起来
        );
        flux.subscribe(s -> System.out.println("subscribe1 " + s));
        flux.subscribe(s -> System.out.println("subscribe2 " + s));

        listener.onEvent("a");
        Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
    }

    /**
     * push: sink#next不存在并发调用情况下使用, 通常是同一个线程产生数据的场景.
     * 和create的区别: create支持并发调用sink#next, 底层通过锁保证数据安全, 但是push没有.
     */
    @Test
    public void push() {
        EventListenerSupport<Observer.EventListener> eventListenerSupport = EventListenerSupport.create(Observer.EventListener.class);
        Observer.EventListener listener = eventListenerSupport.fire();

        Flux<String> flux = Flux.push(fluxSink ->
                eventListenerSupport.addListener(event -> {
                    String threadName = Thread.currentThread().getName();
                    // push: 并发的调用next会有问题
                    new Thread(() -> fluxSink.next(Thread.currentThread().getName() + "&" + threadName + "&" + event)).start();
                })
        );
        flux.subscribe(s -> System.out.println("subscribe1 " + s));
        flux.subscribe(s -> System.out.println("subscribe2 " + s));

        listener.onEvent("a");
        Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
    }

    /**
     * onRequest: 收到subscriber request请求是回调hookOnNext, 在hookOnNext中sink数据的话, 就等同于poll模式了
     */
    @Test
    public void hybridPushAndPull() {
        EventListenerSupport<Observer.EventListener> eventListenerSupport = EventListenerSupport.create(Observer.EventListener.class);
        Observer.EventListener eventListener = eventListenerSupport.fire();

        Flux<String> flux = Flux.create(fluxSink -> {
            eventListenerSupport.addListener(event -> fluxSink.next("push item: " + event)); // push模式
            fluxSink.onRequest(n -> {
                for (int i = 0; i < n / 2; i++) {
                    fluxSink.next("pull item: " + i);
                }
            }); // pull模式: subscribe#request(n)后触发
        });
        flux.subscribeOn(Schedulers.single()).subscribe(new BaseSubscriber<>() {
            @Override
            protected void hookOnSubscribe(Subscription subscription) {
                subscription.request(20);
            }

            @Override
            protected void hookOnNext(String value) {
                System.out.println(value);
            }
        });
        for (int i = 0; i < 10; i++) {
            eventListener.onEvent(String.valueOf(i));
            Uninterruptibles.sleepUninterruptibly(50, TimeUnit.MILLISECONDS);
        }
        Uninterruptibles.sleepUninterruptibly(5, TimeUnit.SECONDS);
    }

    /**
     * Handler是对现有序列的操作, 可以对序列进行转换和跳过, 类似于map+filter操作的结合
     */
    @Test
    public void handler() {
        Flux<String> flux = Flux.range(0, 128).handle((i, sink) -> {
            if (i >= 'A' && i <= 'z') {
                sink.next((char) i.intValue() + " : " + i);
            }
        });
        flux.subscribe(System.out::println);
    }
}
