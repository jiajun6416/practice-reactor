package com.jiajun.reactor.projectreactor;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;
import reactor.util.concurrent.Queues;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @uthor jiajun
 */
public class WindowPublisher<T> {

    private static final int DEFAULT_QUEUE_SIZE = 1 << 4; //default 16

    private final Queue<T> queue;

    private final Sinks.Many<T> sinks;

    private final Flux<List<T>> flux;

    private final int queueCapacity;

    // 失败会重试
    public static final Sinks.EmitFailureHandler ALWAYS_RETRY_HANDLER = (signalType, emitResult) -> emitResult.isFailure();

    /**
     * @param windowMaxBatchSize window聚合最大size
     * @param windowDuration     window聚合最大等待时间
     */
    public WindowPublisher(int windowMaxBatchSize, Duration windowDuration) {
        this(DEFAULT_QUEUE_SIZE, windowMaxBatchSize, windowDuration);
    }

    /**
     * @param windowMaxBatchSize window聚合最大size
     * @param windowDuration     window聚合最大等待时间
     * @param consumerPoolSize   消费者线程池线程个数
     */
    public WindowPublisher(int windowMaxBatchSize, Duration windowDuration, int consumerPoolSize) {
        this(DEFAULT_QUEUE_SIZE, windowMaxBatchSize, windowDuration, consumerPoolSize);
    }

    /**
     * @param queueCapacity      队列容量
     * @param windowMaxBatchSize window聚合最大size
     * @param windowDuration     window聚合最大等待时间
     */
    public WindowPublisher(int queueCapacity, int windowMaxBatchSize, Duration windowDuration) {
        this(queueCapacity, windowMaxBatchSize, windowDuration, Runtime.getRuntime().availableProcessors());
    }

    /**
     * @param queueCapacity      队列容量
     * @param windowMaxBatchSize window聚合最大size
     * @param windowDuration     window聚合最大等待时间
     * @param consumerPoolSize   消费者线程池线程个数
     */
    public WindowPublisher(int queueCapacity, int windowMaxBatchSize, Duration windowDuration, int consumerPoolSize) {
        this.queueCapacity = Queues.ceilingNextPowerOfTwo(queueCapacity);
        this.queue = Queues.<T>get(queueCapacity).get();
        this.sinks = Sinks.many().unicast().onBackpressureBuffer(queue);
        flux = sinks.asFlux()
                .bufferTimeout(windowMaxBatchSize, windowDuration, Schedulers.newSingle("timer"))
                .filter(it -> !it.isEmpty());
    }

    /**
     * 同步发送单个数据
     *
     * @param item 数据项
     */
    public void publish(T item) {
        sinks.emitNext(item, ALWAYS_RETRY_HANDLER);
    }

    /**
     * 同步发送多个数据
     *
     * @param items 集合数据项
     */
    public void publish(Collection<T> items) {
        for (T item : items) {
            publish(item);
        }
    }

    /**
     * 异步发送单个数据
     *
     * @param item 数据项
     */
    public boolean asyncPublish(T item) {
        return sinks.tryEmitNext(item).isSuccess();
    }

    /**
     * 同步发送多个数据
     *
     * @param items 集合数据项
     */
    public Map<T, Boolean> asyncPublish(Collection<T> items) {
        return items.stream().collect(Collectors.toMap(it -> it, this::asyncPublish));
    }

    /**
     * @return 队列元素数量
     */
    public int getQueueSize() {
        return this.queue.size();
    }

    /**
     * @return 队列容量
     */
    public int getQueueCapacity() {
        return this.queueCapacity;
    }

    /**
     * 订阅
     *
     * @param consumer 消费者
     */
    public void subscribe(Consumer<? super Collection<T>> consumer) {
        this.flux.subscribe(consumer);
    }
}
