package com.jiajun.reactor.core;

import com.alibaba.fastjson.JSON;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.Test;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;
import reactor.core.Disposable;
import reactor.core.Exceptions;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
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
        flux.subscribe(new BaseSubscriber<Integer>() {

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
        Flux.range(1, 2).buffer(5).subscribe(System.out::println, null, null, subscription -> subscription.request(2));
        Flux.range(1, 10).buffer(2).subscribe(new BaseSubscriber<List<Integer>>() {
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

        Flux.range(10, 100).limitRate(5).subscribe(System.out::println);
        System.out.println("------------------");

        // 流控, limitRate: push的数量
        Flux.range(10, 10).limitRate(4, 2).subscribe(new BaseSubscriber<Integer>() {
            @Override
            protected void hookOnSubscribe(Subscription subscription) {
                subscription.request(8);
            }

            @Override
            protected void hookOnNext(Integer value) {
                System.out.println(value);
            }
        });
    }

    /**
     * 同步订阅模型, 逐个产生元素
     * 有点像迭代器模型. 将iterator转成stream
     * <p>
     * sink: 产生一个消息给订阅者. sink是flux的另一种数据源(代码方式创建)
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
                    try {
                        TimeUnit.SECONDS.sleep(2);
                    } catch (InterruptedException e) {
                    }
                    return Arrays.asList(1, 2);
                }
        );
        flux2.subscribe(integers -> System.out.println(JSON.toJSONString(integers)));
    }

    /**
     * create:
     * 1. 支持异步, 可以一次产生多个元素
     * 2. 将回调模式转成响应式
     * 3. 支持控制回压
     * <p>
     * 1.
     */
    @Test
    public void create() {
        SettableFuture<String> future = SettableFuture.create();
        Flux<String> flux = Flux.create(fluxSink ->
                future.addCallback(new FutureCallback<String>() {
                    @Override
                    public void onSuccess(@Nullable String result) {
                        // 每轮支持发射多个, 并且可以是不同的线程
                        new Thread(() -> fluxSink.next(Thread.currentThread().getName() + "&" + result)).start();
                        new Thread(() -> fluxSink.next(Thread.currentThread().getName() + "&" + result)).start();
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        fluxSink.error(t);
                    }
                }, MoreExecutors.directExecutor())
        );
        flux.subscribe(System.out::println);

        future.set("a");
    }

    @Test
    public void push() {
        SettableFuture<String> future = SettableFuture.create();
        Flux<String> flux = Flux.push(fluxSink ->
                future.addCallback(new FutureCallback<String>() {
                    @Override
                    public void onSuccess(@Nullable String result) {
                        // 每轮支持发射多个, 并且可以是不同的线程
                        new Thread(() -> fluxSink.next(Thread.currentThread().getName() + "&" + result)).start();
                        new Thread(() -> fluxSink.next(Thread.currentThread().getName() + "&" + result)).start();
                        new Thread(fluxSink::complete).start();
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        fluxSink.error(t);
                    }
                }, MoreExecutors.directExecutor())
        );
        flux.subscribe(System.out::println);

        future.set("a");
    }

    /**
     * onRequest: 收到subscriber request请求时进行回调, 如果此时直接发射消息, 即相当于poll模式
     */
    @Test
    public void hybridPushAndPull() {
        Flux<String> flux = Flux.create(fluxSink -> fluxSink.onRequest(n -> fluxSink.next("obtain source..." + n)));
        flux.subscribe(new BaseSubscriber<String>() {
            @Override
            protected void hookOnSubscribe(Subscription subscription) {
                subscription.request(2);
            }

            @Override
            protected void hookOnNext(String value) {
                System.out.println(value);
            }
        });
    }

    @Test
    public void handler() {
        Flux.range(1, 10).handle((integer, synchronousSink) -> {
            if (integer % 2 == 0) {
                synchronousSink.next(integer * integer);
            }
        }).subscribe(System.out::println);
    }

    /**
     * 线程模型
     */
    @Test
    public void threadModel() throws InterruptedException {
        // single 线程
        Flux.interval(Duration.ofSeconds(1), Schedulers.newSingle("interval", false)).subscribe(System.out::println);

        // publishOn: 指定之后操作使用的线程
        Flux<String> flux = Flux.range(10, 10)
                .map(i -> {
                    System.out.println("map1 thread: " + Thread.currentThread().getName());
                    return 10 + i;
                })
                .publishOn(Schedulers.newParallel("custom-executor"))
                .map(i -> {
                    System.out.println("map2 thread: " + Thread.currentThread().getName());
                    return "value: " + i;
                });
        //new Thread((flux::subscribe)).start();

        // subscribeOn: 指定subscriber的线程, 但是只有subscriber才会触发publisher的计算, 所以操作线程一般都是subscriber线程, 此时会同时被指定.
        // subscribeOn: 和放置顺序无关
        Flux<String> flux2 = Flux.range(10, 10)
                .map(i -> {
                    System.out.println("map1 thread: " + Thread.currentThread().getName());
                    return 10 + i;
                })
                .subscribeOn(Schedulers.newParallel("custom-executor"))
                .map(i -> {
                    System.out.println("map2 thread: " + Thread.currentThread().getName());
                    return "value: " + i;
                });
        // 无论在什么线程中订阅的, 都会转移到subscribeOn指定的线程
        new Thread((flux2::subscribe)).start();

        TimeUnit.SECONDS.sleep(1);
    }

    /**
     * 错误处理: 只要发生错误,序列都是终止的.
     * 1. catch 做fallback,
     * - 返回其他值
     * - 执行异常方法
     * 2. 包装成业务异常
     * 3. finally中进行资源清理
     */
    @Test
    public void errorHandler() throws InterruptedException {
        Function<Integer, Integer> errorFunction = value -> value / (value - 5);

        // subscriber中onError, 此时订阅结束
        Flux.range(1, 10).map(errorFunction).subscribe(System.out::println, error -> System.out.println("catch error: " + error.getMessage()));
        System.out.println("--------------------------------");
        // onErrorReturn: 订阅同样会结束掉
        Flux.range(1, 10).map(errorFunction).onErrorReturn(110).subscribe(System.out::println, error -> System.out.println("catch error: " + error.getMessage()));
        System.out.println("--------------------------------");
        Flux.range(1, 10).map(errorFunction).onErrorReturn(e -> e instanceof ArithmeticException, 110).subscribe(System.out::println, error -> System.out.println("catch error: " + error.getClass().getName()));
        // onErrorResume: 订阅同样会结束掉
        System.out.println("--------------------------------");
        Flux.range(1, 10).map(errorFunction).onErrorResume(e -> Mono.just(110)).subscribe(System.out::println, error -> System.out.println("catch error: " + error.getMessage()));

        // 转换异常: 通过构建一个error的publisher 或 onErrorMap
        System.out.println("--------------------------------");
        Flux.range(1, 10).map(errorFunction).onErrorResume(e -> Mono.error(new RuntimeException(e))).subscribe(System.out::println, error -> System.out.println("catch error: " + error.getMessage())); // 通过map
        Flux.range(1, 10).map(errorFunction).onErrorMap(RuntimeException::new).subscribe(System.out::println, error -> System.out.println("catch error: " + error.getMessage()));

        // doOnXXX: 仅仅回调, 不会带来任何改动(一般用作监控)
        Flux.range(1, 10).map(errorFunction).doOnError(e -> System.out.println("do on error. " + e.getMessage())).subscribe(System.out::println);

        System.out.println("--------------------------------");
        // using: try-with-resource, 用作资源回收
        Flux.using(
                () -> new BufferedReader(new InputStreamReader(new FileInputStream("/script/tbj"))),
                bufferedReader -> Flux.generate(sink -> {
                    String line = null;
                    try {
                        line = bufferedReader.readLine();
                    } catch (IOException e) {
                    }
                    if (line != null) {
                        sink.next(line);
                    } else {
                        sink.complete();
                    }
                }),
                bufferedReader -> {
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {
                    }
                }
        ).subscribe(System.out::println);
        System.out.println("--------------------------------");
        // doFinally: 只要订阅终止即会触发, 包括complete error
        Flux.range(1, 2).doFinally(type -> System.out.println("doFinally: " + type)).subscribe();
        Flux.range(1, 2).doFinally(type -> System.out.println("doFinally: " + type)).take(1).subscribe();
        Flux.range(1, 10).map(errorFunction).doFinally(type -> System.out.println("doFinally: " + type)).subscribe(integer -> {
        }, throwable -> {
        });

        // retry: 重订阅, 会再从头开始处理, retry(n)表示重订阅n次, 如果n次后还失败再丢出异常
        System.out.println("--------------------------------");
        Flux.interval(Duration.ofMillis(250))
                .map(input -> {
                    if (input < 3) return "tick " + input;
                    throw new RuntimeException("boom");
                })
                .retry(1)
                .subscribe(System.out::println, System.err::println);

        Thread.sleep(2100);

        // Exceptions: 对异常进行包装后传递
        System.out.println("--------------------------------");
        Flux.range(1, 10).map(i -> {
            try {
                if (i > 4) {
                    throw new IOException("too large");
                }
                return i;
            } catch (IOException e) {
                throw Exceptions.propagate(e);
            }
        }).subscribe(System.out::println, e -> System.out.println(Exceptions.unwrap(e)));
    }

    /**
     * transform: 将操作链上动作外置
     */
    @Test
    public void transform() {
        // transform: 等同于在链上直接写操作符
        Function<Flux<String>, Publisher<String>> filterAndMap = str -> str.filter(color -> !color.equals("blue")).map(String::toUpperCase);
        Flux.just("blue", "green", "orange", "purple").transform(filterAndMap).subscribe(System.out::println);
        Flux.just("blue", "green", "orange", "purple").filter(color -> !color.equals("blue")).map(String::toUpperCase).subscribe(System.out::println);

        // transformDeferred: 延时执行的transform, 每次订阅过程都会重新再次构建链, 可以实现多次订阅的处理链不同
        // defer概念: 利用延迟将cold -> hot
        System.out.println("--------------------------------");
        AtomicInteger ai = new AtomicInteger();
        filterAndMap = str -> {
            if (ai.incrementAndGet() == 1) {
                return str.filter(color -> !color.equals("orange")).map(String::toLowerCase);
            }
            return str.filter(color -> !color.equals("purple")).map(String::toUpperCase);
        };
        Flux<String> composeFlux = Flux.just("blue", "green", "orange", "purple").transformDeferred(filterAndMap);
        composeFlux.subscribe(System.out::println);
        composeFlux.subscribe(System.out::println);

        // windowing: 可以用来实现metric
        System.out.println("--------------------------------");
        Flux.range(1, 10).window(5).concatMap(integerFlux -> Mono.just(integerFlux.toStream().map(String::valueOf).collect(Collectors.joining(",")))).subscribe(System.out::println);
        // buffer
    }

    /**
     * groupBy返回: Flux<GroupedFlux<K, T>>
     */
    @Test
    public void groupBy() {
        StepVerifier.create(Flux.just(1, 3, 5, 2, 4, 6, 11, 12, 13)
                .groupBy(i -> i % 2 == 0 ? "even" : "odd")
                .concatMap(g -> g.defaultIfEmpty(-1) //如果组为空，显示为 -1
                        .map(String::valueOf) //转换为字符串
                        .startWith(g.key())) //以该组的 key 开头
        )
                .expectNext("odd", "1", "3", "5", "11", "13")
                .expectNext("even", "2", "4", "6", "12")
                .verifyComplete();
    }

    /**
     * window返回Flux<Flux<T>>
     */
    @Test
    public void windowing() {
        // window(maxSize): 收集5个元素关闭窗口, 开启下一个窗口.
        StepVerifier.create(Flux.range(1, 10)
                .window(5)
                .concatMap(g -> g.defaultIfEmpty(-1)) //windows为空 显示为 -1
        )
                .expectNext(1, 2, 3, 4, 5)
                .expectNext(6, 7, 8, 9, 10)
                .verifyComplete();

        // window(int maxSize, int skip)
        StepVerifier.create(Flux.range(1, 10)
                .window(5, 3) // 收集5个元素关闭窗口, 收集3个元素开启窗口. 会层叠2个元素, 如果maxSize>skip则会有元素不在窗口中
                .concatMap(g -> g.defaultIfEmpty(-1)) //windows为空 显示为 -1
        )
                .expectNext(1, 2, 3, 4, 5)
                .expectNext(4, 5, 6, 7, 8)
                .expectNext(7, 8, 9, 10)
                .expectNext(10)
                .verifyComplete();
    }

    /**
     * 指定默认的执行器
     * 修改默认Scheduler为自定义鲜橙汁, 在需要使用额外线程的场景无需自己指定
     */
    @Test
    public void settingDefaultScheduler() {
        // Schedulers中内置了三种常用线程模型, 在不同操作总如果不指定,或者使用以下方式则共用同一个线程池
        Schedulers.setFactory(new Schedulers.Factory() {
            // 修改默认的boundElastic实现
            @Override
            public Scheduler newBoundedElastic(int threadCap, int queuedTaskCap, ThreadFactory threadFactory, int ttlSeconds) {
                return Schedulers.fromExecutorService(new ThreadPoolExecutor(threadCap, threadCap, ttlSeconds, TimeUnit.SECONDS, new LinkedBlockingDeque<>(1000), threadFactory));
            }
        });

        Schedulers.boundedElastic(); // BoundedElasticScheduler: 最大线程数:10*cores, ttl:100000,
        Schedulers.parallel(); // newParallel: 线程数: cores
        Schedulers.single(); // newSingle: 线程数: 1
    }

    /**
     * context是不可变map, 每次修改都会重新创建一个context
     * context是个单独的序列, 其顺序是从下到上
     */
    @Test
    public void context() {
        Mono.just("a")
                .flatMap(s -> Mono.subscriberContext().map(cxt -> cxt.get("key1") + "_" + s))
                .subscriberContext(ctx -> ctx.put("key1", "context1")) // 将context绑定到链路上
                .flatMap(s -> Mono.subscriberContext().map(cxt -> cxt.getOrDefault("key1", "empty") + "_" + s))
                .subscribe(System.out::println);
        Flux.range(1, 10)
                .map(i -> i + "1")
                .subscriberContext(context -> context)
                .map(i -> i + "2")
                .subscribe(System.out::println);

        Mono.just(1)
                .flatMap(i -> Mono.subscriberContext().map(cxt -> i + "" + cxt.get("key1")))
                .subscriberContext(ctx -> ctx.put("key1", "1")) // 将context绑定到链路上
                .map(i -> i + "2")
                .subscribe(System.out::println);

        // 使用Zip将Context合并在序列中
        Mono<String> mono = Mono.just("a").zipWith(Mono.subscriberContext()).map(tuple -> {
            Optional<Object> userOptional = tuple.getT2().getOrEmpty("uid");
            return userOptional.map(o -> "loginUser: " + o).orElse("un login");
        });
        mono.subscriberContext(ctx -> ctx.put("uid", 10086)).subscribe(System.out::println);
    }

}
