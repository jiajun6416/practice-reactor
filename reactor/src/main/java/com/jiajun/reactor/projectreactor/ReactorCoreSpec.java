package com.jiajun.reactor.projectreactor;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.SettableFuture;
import com.google.common.util.concurrent.Uninterruptibles;
import org.apache.commons.collections4.CollectionUtils;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.Test;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;
import reactor.core.Disposable;
import reactor.core.Exceptions;
import reactor.core.publisher.*;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.util.context.Context;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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

        // flux to mono
        Mono<List<Integer>> flux2Mono = Flux.range(1, 10).collect(Collectors.toList());
        // mono to flux
        Flux<String> mono2Flux = Mono.just("hello").concatWith(Mono.just(" reactor"));
    }

    /**
     * 使用Lambda方式进行Subscriber. 支持四个参数
     * - item的Consumer
     * - 错误处理, 错误后触发异常处理, 然后终止
     * - 正常完成后回调
     * - 返回Subscription: Subscription标识一个订阅状态, 可以指定request数量和取消订阅
     */
    @Test
    public void fluxLambdaSubscribe() {
        Flux.range(1, 10).subscribe(
                System.out::print,
                Throwable::printStackTrace,
                () -> System.out.println("complete!"),
                subscription -> subscription.request(20) // Subscription指定拉取的item数
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
     */
    @Test
    public void disposable() {
        Disposable disposable = Flux.just(1, 2, 3, 4).subscribeOn(Schedulers.parallel()).subscribe(System.out::println);
        System.out.println(disposable.isDisposed());
        disposable.dispose();
        System.out.println(disposable.isDisposed());
    }

    /**
     * BaseSubscriber包含了Lambda订阅和Disposable的所有功能
     * 同一个BaseSubscriber实例不能订阅多个publisher, 否则订阅第二个会取消第一个的订阅状态
     */
    @Test
    public void baseSubscriber() {
        Flux<Integer> flux = Flux.range(1, 10);
        flux.subscribeOn(Schedulers.single()).subscribe(new BaseSubscriber<>() {

            @Override
            protected void hookOnSubscribe(Subscription subscription) {
                System.out.println(Thread.currentThread().getName() + ":hookOnSubscribe");
                //request(Long.MAX_VALUE);
                request(5); // 指定请求几个item
            }

            @Override
            protected void hookOnNext(Integer value) {
                System.out.println(Thread.currentThread().getName() + ":hookOnNext:" + value);
            }

            @Override
            protected void hookOnComplete() {
                System.out.println(Thread.currentThread().getName() + ":hookOnComplete");
            }

            @Override
            protected void hookOnError(Throwable throwable) {
                System.out.println(Thread.currentThread().getName() + ":hookOnError:" + throwable);
            }

            @Override
            protected void hookOnCancel() {
                System.out.println(Thread.currentThread().getName() + ":hookOnCancel");
            }

            @Override
            protected void hookFinally(SignalType type) {
                System.out.println(Thread.currentThread().getName() + ":SignalType: " + type);
            }
        });

        Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
    }

    /**
     * #{@link BaseSubscriber#upstream()}: BaseSubscriber中成员变量持有`Subscription`对象, 可以随时拿来使用
     */
    @Test
    public void directOperateSubscription() {
        Flux<Integer> flux = Flux.range(1, 10);
        flux.subscribe(new BaseSubscriber<>() {

            @Override
            protected void hookOnSubscribe(Subscription subscription) {
                System.out.println(Thread.currentThread().getName() + ":hookOnSubscribe");
                request(1); // 初始获取一个
            }

            @Override
            protected void hookOnNext(Integer value) {
                System.out.println(Thread.currentThread().getName() + ":hookOnNext:" + value);
                Subscription subscription = upstream(); // 直接获取Subscription对象再次操作
                subscription.request(1); // 每次onNext处理后接着request
            }

            @Override
            protected void hookOnComplete() {
                System.out.println(Thread.currentThread().getName() + ":hookOnComplete");
            }

            @Override
            protected void hookOnError(Throwable throwable) {
                System.out.println(Thread.currentThread().getName() + ":hookOnError:" + throwable);
            }

            @Override
            protected void hookOnCancel() {
                System.out.println(Thread.currentThread().getName() + ":hookOnCancel");
            }
        });
    }

    /**
     * 回压: 即request(xx), 以下几种方式订阅的时候都是request(Long.maxValue), 不限制push的速度, 即禁用了回压, 此时publisher是不会被阻塞住!
     * 1. subscribe方式默认
     * 2. block
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
     * 除在Subscribe中控制消费, 还能在中间操作符控制消费的数量和速率
     * limitRequest: 每个Subscribe只能获取n个元素
     * limitRate: 每个Subscribe的消费速率
     */
    @Test
    public void limitAndLimitRate() {
        // limitRequest: 标示只消费x个
        Flux.range(10, 10).limitRequest(5).subscribe(System.out::println);
        System.out.println("------------------");

        // 流控, limitRate: push的数量
        Flux.range(10, 20).limitRate(4).subscribe(new BaseSubscriber<>() {
            @Override
            protected void hookOnSubscribe(Subscription subscription) {
                subscription.request(10);
            }

            @Override
            protected void hookOnNext(Integer value) {
                System.out.println(value);
            }
        });
    }

    /**
     * Scheduler: 线程模型
     * publishOn: 指定发射的线程
     * subscribeOn: 指定subscribe的线程(常用)
     */
    @Test
    public void scheduler() throws InterruptedException {
        // single 线程
        Flux.interval(Duration.ofSeconds(1), Schedulers.newSingle("interval", false)).subscribe(System.out::println);

        // publishOn: 指定之后操作使用的线程, 如果subscribe和publish是一个线程, 则subscribe的线程也会改变
        Flux<String> flux = Flux.just(1)
                .map(i -> {
                    System.out.println("map1 thread: " + Thread.currentThread().getName());
                    return 10 + i;
                })
                .publishOn(Schedulers.newParallel("custom1-executor"))
                .map(i -> {
                    System.out.println("map2 thread: " + Thread.currentThread().getName());
                    return "value: " + i;
                });
        //new Thread(() -> flux.subscribe(s -> System.out.println("subscribe thread: " + Thread.currentThread().getName()))).start();

        Mono<Integer> mono = Mono.create(sink -> new Thread(() -> sink.success(1)).start());
        mono.map(i -> {
            System.out.println("map1 thread: " + Thread.currentThread().getName());
            return 10 + i;
        }).subscribeOn(Schedulers.newParallel("custom2-executor"))
                .map(i -> {
                    System.out.println("map2 thread: " + Thread.currentThread().getName());
                    return "value: " + i;
                });
        // 无论在什么线程中订阅的, 都会转移到subscribeOn指定的线程
        new Thread(() -> mono.subscribe(s -> System.out.println("subscribe thread: " + Thread.currentThread().getName()))).start();
        // subscribeOn: 指定subscriber的线程, 但是只有subscriber才会触发publisher的计算, 所以操作线程一般都是subscriber线程, 此时会同时被指定.
        // subscribeOn: 和放置顺序无关

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
        Function<Long, Long> errorFunction2 = value -> value / (value - 5);

        // subscriber中onError, 此时订阅结束
        Flux.range(1, 10).map(errorFunction).subscribe(System.out::println, error -> System.out.println("catch error: " + error.getMessage()));
        System.out.println("--------------------------------");
        // onErrorReturn: 不触发error, 但是订阅同样会结束掉
        Flux.range(1, 10).map(errorFunction).onErrorReturn(110).subscribe(System.out::println, error -> System.out.println("catch error: " + error.getMessage()));
        System.out.println("--------------------------------");
        Flux.range(1, 10).map(errorFunction).onErrorReturn(e -> e instanceof ArithmeticException, 110).subscribe(System.out::println, error -> System.out.println("catch error: " + error.getClass().getName()));
        // onErrorResume: 不触发error, 订阅同样会结束掉
        System.out.println("--------------------------------");
        Flux.range(1, 10).map(errorFunction).onErrorResume(e -> Mono.just(110)).subscribe(System.out::println, error -> System.out.println("catch error: " + error.getMessage()));

        // 转换异常: 通过构建一个error的publisher 或 onErrorMap.(通常是推荐该方式)
        System.out.println("--------------------------------");
        Flux.range(1, 10).map(errorFunction).onErrorResume(e -> Mono.error(new RuntimeException(e))).subscribe(System.out::println, error -> System.out.println("catch error: " + error.getMessage())); // 通过map
        Flux.range(1, 10).map(errorFunction).onErrorMap(RuntimeException::new).subscribe(System.out::println, error -> System.out.println("catch error: " + error.getMessage()));

        // doOnXXX: 仅仅回调, 不会带来任何改动(一般用作监控)
        Flux.range(1, 10).map(errorFunction).doOnError(e -> System.out.println("do on error. " + e.getMessage())).subscribe(System.out::println);

        System.out.println("--------------------------------");
        // using: try-with-resource, 用作资源回收
        Flux.using(
                () -> new BufferedReader(new InputStreamReader(new FileInputStream("/etc/hosts"))),
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
        Flux.interval(Duration.ofMillis(250)).map(errorFunction2).retry(1)
                .subscribe(System.out::println, System.out::print);

        Thread.sleep(2100);

        // Exceptions: 对异常进行包装后传递
        System.out.println("--------------------------------");
        Flux.range(1, 10).map(errorFunction).onErrorMap(Exceptions::propagate).subscribe(System.out::println, e -> System.out.println(Exceptions.unwrap(e)));
    }

    /**
     * transform: 将操作连当成一个变量外置
     * transform+deferred: 延迟操作连, 可以实现同一个流使用不同的操作连
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
     * **传播（Propagation） + 不可变性（immutability）**
     * <p>
     * 绑定: 操作.subscriberContext(ctx->)
     * 读取: 静态方法Mono#subscriberContext, 只会读取最近(下面)的context
     * 特性:
     * - 绑定subscribe,基于Subscription传播(由下往上)
     * - 不可变性: context每次修改都会返回一个新对象(put会把之前的内容putAll新context对象), subscriberContext对context的修改是不会互相影响的
     */
    @Test
    public void context() {
        String key = "key1";
        // 绑定context: 这样每个操作符(Reactor内置的操作符)都可以访问, 本质是基于`Subscription`的传播特性实现(从subscribe往上)
        Flux.range(1, 10).subscriberContext(ctx -> ctx.put(key, "context1")).subscribe();

        // 不可变性: Context#put每次都会返回一个新的Context对象, 并且把之前Context中的内容copy进新的context中
        // 由于每次修改都是返回一个新的Context, 所以subscriberContext是不会互相影响的
        Flux.range(1, 2)
                .subscriberContext(origin -> {
                    System.out.println(System.identityHashCode(origin));
                    Context after = origin.put(key, "context1");
                    System.out.println(System.identityHashCode(after));
                    return after;
                })
                .subscribe(System.out::print);

        // 读取context
        // Mono#subscriberContext. 由于由下而上+不可变性(每次变动都是新对象), 在绑定context后面的操作符是获取不到context的
        Mono.just("a")
                .flatMap(s -> Mono.subscriberContext().map(cxt -> cxt.get("key1") + "_" + s)) // 读取
                .subscriberContext(ctx -> ctx.put("key1", "context1"))
                .flatMap(s -> Mono.subscriberContext().map(cxt -> cxt.getOrDefault("key1", "empty") + "_" + s)) // 读取不到
                .subscribe(System.out::println);

        // Mono.subscriberContext()返回的是Mono, 使用的时候必须使用flat. 使用flatMap或者zipWith
        Mono.just("a")
                .flatMap(it -> Mono.subscriberContext().map(ctx -> ctx.getOrEmpty("uid").map(uid -> "loginUser: " + uid).orElse("un login")))
                .subscriberContext(context -> context.put("uid", 10086))
                .subscribe(System.out::println);

        Mono.just("a")
                .zipWith(Mono.subscriberContext())
                .map(tuple -> {
                    Optional<Object> userOptional = tuple.getT2().getOrEmpty("uid");
                    return userOptional.map(o -> "loginUser: " + o).orElse("un login");
                }).subscriberContext(ctx -> ctx.put("uid", 10086))
                .subscribe(System.out::println);

        // 只能读取到离当前操作符最近(下面)的context. 记住由于context的不可变性, 每次subscriberContext不会影响之前的subscriberContext
        Mono.just("Hello")
                .flatMap(s -> Mono.subscriberContext().map(ctx -> s + " " + ctx.get(key))) // 读取第二次赋值: hello Reactor
                .subscriberContext(ctx -> ctx.put(key, "Reactor")) // 读取不到
                .flatMap(s -> Mono.subscriberContext().map(ctx -> s + " " + ctx.get(key))) // 读取第一次赋值: hello Reactor World
                .subscriberContext(ctx -> ctx.put(key, "World")) // 第一次赋值, 返回一个新的context
                .subscribe(System.out::println); // Hello Reactor World

        // 内部Context和外部隔离
        Mono.just("Hello").flatMap(s -> Mono.subscriberContext().map(ctx -> s + " " + ctx.get(key))) // 读取到的Context还是 外部context
                .flatMap(s ->
                        Mono.subscriberContext().map(ctx -> s + " " + ctx.get(key)).subscriberContext(ctx -> ctx.put(key, "Reactor")) // 内部Context不影响外部
                )
                .subscriberContext(ctx -> ctx.put(key, "World")) // 外部context
                .subscribe(System.out::println); // Hello World Reactor
    }

    /**
     * 创建序列API
     */
    @Test
    public void createApi() {
        // just
        Flux.just(1, 3, 4);
        // 延迟特性
        Mono.fromSupplier(() -> 1);
        //基于迭代数据结构
        Flux.fromArray(new String[]{"a", "b", "c"});
        Flux.fromStream(Stream.of(1, 2, 3, 4));
        Flux.fromStream(() -> Stream.of(1, 2, 3, 4));
        Flux.fromIterable(() -> Arrays.asList(1, 2, 3).iterator());

        // 异步结果
        Mono.fromFuture(CompletableFuture.completedFuture(1));

        // 立即生成异常
        Mono.error(Exception::new);

        // 可编程生成: 同步
        Flux.generate(
                () -> 1,
                (status, sink) -> {
                    if (status > 10) {
                        sink.complete();
                    }
                    sink.next(status);
                    return status++;
                });

        // 可编程生成: 支持异步
        // Flux.push()

        Mono.create(
                sink -> Futures.addCallback(SettableFuture.create(), new FutureCallback<Object>() {
                    @Override
                    public void onSuccess(@Nullable Object result) {
                        sink.success(result);
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        sink.error(t);
                    }
                })
        );
    }

    @Test
    public void map() {
        Mono<String> mapMono = Mono.just(1).map(String::valueOf);

        // cast
        Mono<Integer> castMono = Mono.just(1).cast(Integer.class);
        // 获取index
        Flux<Tuple2<Long, Integer>> valueIndex = Flux.range(1, 10).index();

        // handle: 任意转换, 单个值/多个值/异常. 更加灵活. handle也是通过sink实现
        Mono<String> handleMap = Mono.just(1).handle((integer, synchronousSink) -> synchronousSink.next("a")).cast(String.class);

        // startWith: 从指定值开始的子序列
        Flux.range(1, 10).startWith(5);

        // flatMap: 扁平多个publisher
        Mono<Integer> flatMap = Mono.just(1).flatMap(i -> Mono.just(i * i));
        // flatMap保留原本顺序
        Flux<Integer> flatMapSequential = Flux.range(1, 10).flatMapSequential(i -> Mono.just(i * i));

        // flux -> 集合的mono
        Mono<List<Integer>> flux2List = Flux.range(1, 100).collectList();
        Mono<Map<Integer, Integer>> flux2Map = Flux.range(1, 100).collectMap(Function.identity(), Function.identity());
        Mono<List<Integer>> flux2SortList = Flux.range(1, 100).collectSortedList();
        Flux.range(1, 100).collect(Collectors.toList()); // 参考java8中的集合

        // 计数
        Mono<Long> fluxCount = Flux.range(1, 100).count();

        // 合并publisher: concat, 按照顺序分别运行，flux1运行完成以后再运行flux2
        Flux.concat(Flux.range(1, 10), Flux.range(10, 10), Flux.range(20, 10)).subscribe(System.out::print);
        Flux.range(1, 10).concatMap(integer -> Flux.range(1, integer)).subscribe(System.out::print); // 等同于Map+concat
        Flux.concatDelayError(Flux.range(1, 10), Flux.range(10, 10), Flux.range(20, 10)); // delayError:等所有的合并完成后再丢Error

        // 合并publisher: merge,同时运行，根据时间先后运行, 会出现混合
        Flux.merge(Flux.range(1, 10), Flux.range(10, 10), Flux.range(20, 10))
                .subscribe(System.out::print);// 按照发射顺序合并
        Flux.mergeSequential(Flux.range(1, 10), Flux.range(10, 10), Flux.range(20, 10)); // sequential: 按照订阅的顺序合并?

        // zip: 将多个元素打包成 返回Tuple
        Flux<Tuple3<Integer, Integer, Integer>> zip3 = Flux.zip(Flux.range(1, 10), Flux.range(10, 10), Flux.range(20, 10));

        // 代替空序列
        Mono.empty().defaultIfEmpty("defaultIfEmpty").subscribe(System.out::println);
        Mono.empty().switchIfEmpty(Mono.just("defaultIfEmpty")).subscribe(System.out::println); // 使用默认的publisher代替

        //  Mono<Void>导致后续操作没生效, then()会返回Mono.empty
        // then(): 丢弃结果, 只关心是否结束
        Mono.empty().zipWith(Mono.just(1)).subscribe(tuple -> System.out.println(tuple.getT2())); // 没有序列不会触发zipWith
        Mono.empty().zipWhen(o -> Mono.just(1)).subscribe(tuple -> System.out.println(tuple.getT2())); //没有序列不会触发zipWhen
        Mono.just(100).zipWith(Mono.empty()).subscribe(tuple -> System.out.println(tuple.getT1())); //zipWith一个空序列同样不会触发subscribe
        Mono.just(1).then().zipWhen(o -> Mono.just(1)).subscribe(tuple -> System.out.println(tuple.getT2())); //没有序列不会触发zipWhen
        Mono.empty().defaultIfEmpty(0).zipWith(Mono.just(1)).subscribe(tuple -> System.out.println(tuple.getT2())); // 触发zipWith
    }

    /**
     * doOn是只读操作, 不影响序列. 一般用来记录日志
     * 通常有以下几种类型
     * ON_COMPLETE: 正常完成
     * ON_ERROR: 异常完成
     * ON_CANCEL: 取消订阅
     * <p>
     * 很多时候只关心是否终止: doFinally, 所有的退出都会回调
     */
    @Test
    public void doOn() {
        // doOneNext
        // doOnComplete
        // doOnCancel
        // doOnSubscribe
        // doOnRequest
        // doFinally
    }

    /**
     * https://htmlpreview.github.io/?https://github.com/get-set/reactor-core/blob/master-zh/src/docs/index.html#which.filtering
     * https://projectreactor.io/docs/core/release/reference/index.html#which.filtering
     * 相比stream, flux中的序列操作更加强大
     * filter: 会对比每个元素
     * - until/while: 相当于一个开关, 只要某个位置满足/不满足, 后续都不再对别. 相当于 any操作
     * xxxUntil: 一直操作,当条件满足时结束该操作
     * xxxWhile: 条件满足时操作, 当某个元素导致条件不满足则结束该操作
     */
    @Test
    public void filter() throws IOException {
        // filter: 通过指定的条件过滤
        Flux.range(1, 100).filter(i -> i % 2 == 0);
        Flux.range(1, 100).filterWhen(i -> Mono.just(i % 2 == 0)); // 可以异步判断

        // 去重
        Flux.range(1, 100).distinct(); // 整体序列去重
        Flux.range(1, 100).distinctUntilChanged(); // 只去重连续重复的元素

        // 取一部分元素
        Flux.range(1, 10).take(20).subscribe(System.out::print); // 取前n个元素
        Flux.range(1, 10).takeLast(20).subscribe(System.out::print); // 取最后n个元素
        Flux.range(1, 100).next(); // 取第一个元素放入mono

        Flux.just(1, 5, 6, 3, 4).takeUntil(i -> i > 5).subscribe(System.out::print); // 满足条件后不再take
        Flux.just(1, 5, 6, 3, 4).takeWhile(i -> i <= 5).subscribe(System.out::print); // 只要不满足条件即不再take

        // 只取一个元素
        Flux.range(1, 10).elementAt(2);
        Flux.range(1, 10).takeLast(1);
        Flux.range(1, 10).last();
        Flux.range(1, 10).last(0); // 不存在设置默认值

        // 跳过
        Flux.range(1, 10).skip(2); // 跳过前面几个
        Flux.range(1, 10).skip(Duration.ofSeconds(2)); // 跳过指定时间内
        Flux.range(1, 10).skipLast(2); // 跳过最后几个
        System.out.println();
        Flux.just(1, 5, 6, 3, 4).skipUntil(i -> i > 5).subscribe(System.out::print); // 跳过直到满足某个条件, 后续都不跳过
        System.out.println();
        Flux.just(1, 5, 6, 3, 4).skipWhile(i -> i <= 5).subscribe(System.out::print); // 符合条件的都跳过, 后续都不跳过

        // 采样, 可以用来做窗口统计
        System.out.println();
        Flux.interval(Duration.ofMillis(100))
                .take(Duration.ofSeconds(2)) // 只取2s内的值
                //.sample(Duration.ofSeconds(1)) // 采样周期
                .subscribe(System.out::print);

        System.in.read();
    }

    /**
     * 错误处理
     */
    @Test
    public void errorProcessor() {
        // 创建错误序列
        Flux<Object> errorFlux = Flux.error(new RuntimeException());
        Flux.concat(errorFlux);
        Flux.just(1).then(Mono.error(new RuntimeException()));

        // 使用try-catch-finally. onError: catch后的处理. doFinally: finally处理
        Flux.just(1).then(Mono.error(new RuntimeException()))
                .onErrorReturn(2) // catch后返回默认值
                // .onErrorResume(error -> Mono.just(3)) // catch后返回默认publisher(其他计算)
                .onErrorMap(Exceptions::propagate)  // 将异常进行转换, 可以使用Exceptions对异常进行包装
                .doFinally(signalType -> System.out.println("finally type: " + signalType)) // finally代码块, 能获取到序列是如何完成的
                .subscribe(System.out::println);
        // using: try-with-resource

        // 处理回压错误: 上游push>下游request
    }

    /**
     * 基于时间的操作
     */
    @Test
    public void timeAndDelay() throws IOException {
        // elapsed: 当前消息距离上个消息过去的时间, 单位ms
        Flux.interval(Duration.ofSeconds(1)).elapsed().take(10).subscribe(tuple -> System.out.println("ts1: " + tuple.getT1() + ", value1: " + tuple.getT2()));
        // timestamp: 带上每个消息产生的时间戳
        Flux.interval(Duration.ofSeconds(1)).timestamp().take(10).subscribe(tuple -> System.out.println("ts2: " + tuple.getT1() + ", value2: " + tuple.getT2()));

        // delay: 延时发射
        Mono.just("delayElement").delayElement(Duration.ofSeconds(2)).subscribe(System.out::println);

        // delaySubscription: 延时订阅
        Mono.just("delaySubscription").delaySubscription(Duration.ofSeconds(2)).subscribe(System.out::println);

        // timout: 指定序列发射的超时
        Mono.just("delayElement-timeout").delayElement(Duration.ofSeconds(2)).timeout(Duration.ofSeconds(1)).doOnError(System.out::println).subscribe(System.out::println);

        // defer: lazy特性

        // timeout: 底层使用的ScheduledService, 延时队列DelayQueue
        CompletableFuture<Integer> future = new CompletableFuture<>();
        Mono.fromFuture(future)
                .timeout(Duration.ofSeconds(1), Mono.just(2))
                .subscribe(System.out::println, Throwable::printStackTrace);
        new Thread(() -> {
            try {
                TimeUnit.SECONDS.sleep(2);
                future.complete(1);
            } catch (InterruptedException e) {
            }
        }).start();

        System.in.read();
    }

    /**
     * 序列转成窗口: Flux<T> -> Flux<Flux<T>>.
     * - 窗口生成维度
     * 1. 元素个数
     * 2. 时间
     * 3. 个数/时间
     * 4. 元素的条件
     * - n窗口重叠现象(适用于滑动窗口): 窗口宽度>两个窗口的间隔, 则会出现部分数据在两个窗口中. 窗口宽度>>两个窗口的间隔, 会同时存在很多窗口
     */
    @Test
    public void splitWindow() throws IOException {
        BaseSubscriber<Flux<Integer>> windowSubscribe = new BaseSubscriber<Flux<Integer>>() {

            @Override
            protected void hookOnNext(Flux<Integer> value) {
                value.subscribe(new BaseSubscriber<Integer>() {
                    @Override
                    protected void hookOnSubscribe(Subscription subscription) {
                        System.out.println("Start window");
                        subscription.request(Long.MAX_VALUE);
                    }

                    @Override
                    protected void hookOnNext(Integer value) {
                        System.out.println(value);
                    }

                    @Override
                    protected void hookOnComplete() {
                        System.out.println(String.format("Finish window"));
                    }
                });
            }
        };

        // maxSize: 单个窗口的宽度. skip: 两个窗口的数量间隔.
        // Flux.range(1, 100).window(10).subscribe(windowSubscribe);
        // Flux.range(1, 100).window(10, 5).subscribe(windowSubscribe);

        // windowingTimespan:单个窗口的宽度,  openWindowEvery: 两个窗口的时间间隔
        // Flux.interval(Duration.ofMillis(100)).map(Long::intValue).window(Duration.ofSeconds(1)).subscribe(windowSubscribe);
        // Flux.interval(Duration.ofMillis(100)).map(Long::intValue).window(Duration.ofSeconds(1), Duration.ofMillis(500)).subscribe(windowSubscribe);

        // windowTimeout: window大小满足指定个数/指定时间
        Flux.interval(Duration.ofMillis(100)).map(Long::intValue).windowTimeout(9, Duration.ofSeconds(1)).subscribe(windowSubscribe);

        // windowUntil: 基于条件的窗口. todo
        System.in.read();
    }

    /**
     * Flux<T> -> Flux<List<T>> request(2)表示每次请求两个buffer
     * buffer和window类似:
     * 1. size维度buffer
     * 2. 时间维度buffer
     * 3. size/时间维度
     * 4. 条件判断维度
     */
    @Test
    public void splitBuffer() {
        Flux.interval(Duration.ofMillis(100)).buffer(10);
        Flux.interval(Duration.ofMillis(100)).buffer(Duration.ofSeconds(1));
        Flux.interval(Duration.ofMillis(100)).bufferTimeout(10, Duration.ofSeconds(1)); // 十个或者1s
        Flux.interval(Duration.ofMillis(100)).bufferUntil(aLong -> aLong < 100);
    }

    /**
     * groupBy返回: Flux<GroupedFlux<K, T>>
     */
    @Test
    public void splitGroupBy() {
        Flux.just(1, 3, 5, 2, 4, 6, 11, 12, 13)
                .groupBy(i -> i % 2 == 0 ? "even" : "odd")
                .subscribe(new BaseSubscriber<GroupedFlux<String, Integer>>() {
                    @Override
                    protected void hookOnNext(GroupedFlux<String, Integer> value) {
                        System.out.println(value.key());
                        value.subscribe(System.out::println);
                    }
                });
    }

    /**
     * 同步获取所有序列. 阻塞式流处理(等待消费完所有的item!)
     * - block
     * - toIterator / toStream
     * -  toFuture
     */
    @Test
    public void publisherToBlock() {
        Mono.defer(() -> Mono.just(1)).block();
        Flux.range(1, 100).toIterable();
        Mono.just(2).toFuture();
        Mono.just(1).then(); // then: 等待mono结束
    }

    /**
     * batchSize: 批量消费模式下的对象缓存!
     */
    @Test
    public void fluxToStreamOrIterator() {
        Flux<List<Integer>> flux = Flux.generate(() -> 0, (idx, sink) -> {
            if (idx > 1000) {
                sink.complete();
                return idx + 1;
            }
            List<Integer> item = IntStream.range(0, 100).boxed().collect(Collectors.toList());
            sink.next(item);
            return idx + 1;
        });
        int batchSize = 256; // 批量消费batchSize个元素后, 再进行迭代遍历. 会存在batchSize个元素的缓存. 如果每个item占用空间很大的话, 会造成OOM
        // batchSize = 1; // batchSize=1时等同于使用迭代器.
        flux.toStream(batchSize).forEach(System.out::println);
    }

    /**
     * java中Iterator转成Stream, 只支持单个消费模式, 不存在Item缓存的问题, 即不存在OOM的风险.
     */
    @Test
    public void iteratorToStream() {
        // 内部是: 先调用hasNext, 再调用next
        Spliterator<List<Integer>> spliterator = Spliterators.spliteratorUnknownSize(new Iterator<>() {
            int idx;

            List<Integer> item;

            @Override
            public boolean hasNext() {
                if (idx > 1000) {
                    return false;
                }
                item = IntStream.range(0, 100).boxed().collect(Collectors.toList());
                return true;
            }

            @Override
            public List<Integer> next() {
                if (CollectionUtils.isEmpty(item)) {
                    throw new UnsupportedOperationException();
                }
                idx++;
                return item;
            }
        }, Spliterator.NONNULL/*非空*/);
        StreamSupport.stream(spliterator, false/*同步*/).forEach(System.out::println);
    }
}
