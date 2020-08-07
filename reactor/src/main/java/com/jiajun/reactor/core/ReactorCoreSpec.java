package com.jiajun.reactor.core;

import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.reactivestreams.Subscription;
import reactor.core.Disposable;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * @author jiajun
 */
public class ReactorCoreSpec {

    /**
     * 工厂方式创建Flux/Moon, 本质就是提前确定好消息源
     */
    @Test
    public void fluxAndMonoFactory() {
        Flux<String> flux1 = Flux.just("foot", "bar", "foobar");
        Flux<Integer> flux2 = Flux.fromStream(IntStream.rangeClosed(1, 10).boxed());
        Flux<Integer> flux3 = Flux.range(1, 10);

        Mono<String> emptyMono = Mono.empty();
        Mono<String> mono1 = Mono.just("foo");
    }

    /**
     * 使用Lambda方式进行Subscriber
     */
    @Test
    public void fluxLambdaSubscribe() {
        Flux.range(1, 10).subscribe(
                System.out::print,
                Throwable::printStackTrace,
                () -> System.out.println("complete!"),
                subscription -> subscription.request(20) // Subscription指定拉取的iterm数
        );

        Flux.range(1, 10).subscribe(
                System.out::print,
                Throwable::printStackTrace,
                () -> System.out.println("complete!"),
                Subscription::cancel // 不push消息
        );

        System.out.println("\r-------------");
        Flux<Integer> ints = Flux.range(1, 4)
                .map(i -> {
                    if (i <= 3) return i;
                    throw new RuntimeException("Got to 4");
                });
        //ints.subscribe(System.out::println);
        ints.subscribe(System.out::println,
                error -> System.err.println("Error: " + error));
    }

    @Test
    public void monoLambdaSubscribe() {
        Mono.fromFuture(() -> CompletableFuture.completedFuture("foo")).subscribe(System.out::println);
        Mono.just("foo").subscribe(
                System.out::print,
                Throwable::printStackTrace,
                () -> System.out.println("complete!")
                //subscription -> subscription.cancel() // 不push消息
        );
    }

    /**
     * Lambda方式订阅的话, 返回的Disposable (`可以被取消或者销毁`)
     * 通过Disposable可以取消订阅
     * 如果消息处理的很快, 不保证能够cancel成功!
     */
    @Test
    public void disposable() {
        // operator很快的话,则不一定能取消成功
        Disposable disposable = Flux.just(1, 2, 3, 4).subscribe(System.out::println);
        System.out.println(disposable.isDisposed());
        disposable.dispose();
        System.out.println(disposable.isDisposed());

        System.out.println("------------");

        Disposable disposable2 = Flux.just(1, 2, 3, 4).flatMap(integer -> {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
            }
            return Mono.just(integer);
        }).subscribe(System.out::println);
        System.out.println(disposable2.isDisposed());
        disposable2.dispose();
        System.out.println(disposable2.isDisposed());
    }

    /**
     * 使用BaseSubscriber代替Lambda的subscribe方式, BaseSubscriber包含了Lambda订阅和Disposable的所有功能
     * 同一个BaseSubscriber实例不能同时subscribe多个publisher, 订阅第二个会把取消第一个的订阅状态
     */
    @Test
    public void baseSubscriber() {
        Flux<Integer> flux = Flux.range(1, 10);
        flux.subscribe(new BaseSubscriber<>() {

            @Override
            protected void hookOnSubscribe(Subscription subscription) {
                System.out.println("onSubscribe");
                subscription.request(1);
            }

            @Override
            protected void hookOnNext(Integer value) {
                System.out.println(value);
                request(1);
            }

            @Override
            protected void hookOnComplete() {
                System.out.println("complete");
            }

            @Override
            protected void hookOnError(Throwable throwable) {
                System.err.println("Error: " + throwable);
            }
        });
    }

    /**
     *
     * 回压: 即request(xx), 以下几种方式订阅的时候都是request(Long.maxValue), 不限制push的速度, 即禁用了回压, 此时publisher是不会被阻塞住!
     * 1. subscribe方式默认
     * 2/ block
     * 3. toStream / toIterable
     */
    @Test
    public void backpressureLongValue() {
        // lambda
        Flux.range(1, 10).subscribe(System.out::println);
        // block
        System.out.println(Flux.range(1, 10).blockFirst());

        // toIterable toStream
        Flux.range(10, 10).toStream().forEach(System.out::print);
    }

    /**
     * 重塑消费者的需求
     * buffer会将上游item缓存成集合
     * request(2)表示每次请求两个buffer
     */
    @Test
    public void buffer() {
        Flux.range(1, 10).buffer(5).subscribe(
                System.out::println,
                null,
                null,
                subscription -> subscription.request(2)
        );
        Flux.range(1, 10)
                .buffer(2)
                .subscribe(new BaseSubscriber<List<Integer>>() {
                    @Override
                    protected void hookOnSubscribe(Subscription subscription) {
                        subscription.request(2);
                    }

                    @Override
                    protected void hookOnNext(List<Integer> value) {
                        System.out.println(value);
                    }
                });
    }

    /**
     *
     */
    @Test
    public void limit() {
        // limitRequest: 标示只消费x个
        Flux.range(10, 10).limitRequest(5).subscribe(System.out::println);

        System.out.println("------------------");

        // 流控, limitRate: 每s push的数量
        Flux.range(10, 10).limitRate(10, 5).subscribe(new BaseSubscriber<>() {
            @Override
            protected void hookOnSubscribe(Subscription subscription) {
                subscription.request(6);
            }


            @Override
            protected void hookOnNext(Integer value) {
                System.out.println(value);
            }
        });
    }

    /**
     * 有点像迭代器模型. 将iterator转成stream
     * <p>
     * sink: 产生一个消息给订阅者.
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
                    try {
                        TimeUnit.SECONDS.sleep(2);
                    } catch (InterruptedException e) {
                    }
                    return Arrays.asList(1, 2);
                }
        );
        flux2.subscribe(integers -> System.out.println(JSON.toJSONString(integers)));
    }
}
