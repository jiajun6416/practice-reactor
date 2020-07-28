package com.jiajun.reactor.core;

import org.junit.Test;
import org.reactivestreams.Subscription;
import reactor.core.Disposable;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * @author jiajun
 */
public class ReactorCoreSpec {

    @Test
    public void fluxAndMonoFactory() {
        Flux<String> flux1 = Flux.just("foot", "bar", "foobar");
        Flux<Integer> flux2 = Flux.fromStream(IntStream.rangeClosed(1, 10).boxed());
        Flux<Integer> flux3 = Flux.range(1, 10);

        Mono<String> emptyMono = Mono.empty();
        Mono<String> mono1 = Mono.just("foo");
    }

    @Test
    public void fluxSubscribe() {
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
                subscription -> subscription.cancel() // 不push消息
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
    public void monoSubscribe() {
        Mono.fromFuture(() -> CompletableFuture.completedFuture("foo")).subscribe(System.out::println);
        Mono.just("foo").subscribe(
                System.out::print,
                Throwable::printStackTrace,
                () -> System.out.println("complete!")
                //subscription -> subscription.cancel() // 不push消息
        );
    }

    /**
     * 通过Disposable可以取消订阅
     * 如果消息处理的很快, 不保证能够cancel成功
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
     * 使用BaseSubscriber代替Lambda的subscribe方式
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
     * 回压: Long.maxValue的速率, 不限制push的速度, 即禁用了回压, 此时publisher是不会被阻塞住!
     */
    @Test
    public void backpressureLongValue() {
        // lambda
        Flux.range(1, 10).subscribe(System.out::println);
        // block
        System.out.println(Flux.range(1, 10).blockFirst());

        // toIterable toStream
        Flux.range(1, 10).toIterable().forEach(System.out::print);
        Flux.range(1, 10).toStream().forEach(System.out::print);
    }
}
