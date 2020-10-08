package com.jiajun.practice.reactor.webflux.util;

import com.ximalaya.mainstay.common.InvokerHelper;
import com.ximalaya.mainstay.common.concurrent.Future;
import com.ximalaya.mainstay.common.concurrent.FutureCallback;
import com.ximalaya.mainstay.common.concurrent.Futures;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * @author jiajun
 */
@Slf4j
public class MonoUtil {

    /**
     * Future -> Mono
     *
     * @param future
     * @param <T>
     * @return
     */
    public static <T> Mono<T> from(Future<T> future) {
        return Mono.create(sink -> Futures.addCallback(future,
                new FutureCallback<T>() {
                    @Override
                    public void onSuccess(T result) {
                        sink.success(result);
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        sink.error(t);
                    }
                }));
    }

    /**
     * Future -> Mono, 支持设置动态超时
     *
     * @param endTimeStamp:  执行终止时间戳
     * @param futureSupplier
     * @param <T>
     * @return
     */
    public static <T> Mono<T> from(long endTimeStamp, Supplier<Future<T>> futureSupplier) {
        long timeout = endTimeStamp - System.currentTimeMillis();
        if (timeout <= 0) {
            return Mono.error(new IllegalArgumentException("timeout [" + timeout + "] less then 0"));
        }
        InvokerHelper.setNcTimeout((int) timeout); // 动态超时
        return Mono.create(sink -> Futures.addCallback(futureSupplier.get(),
                new FutureCallback<T>() {
                    @Override
                    public void onSuccess(T result) {
                        sink.success(result);
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        sink.error(t);
                    }
                }));
    }

    /**
     * Future -> Mono, 支持设置动态超时
     * 超时返回 Optional.empty
     *
     * @param endTimeStamp:  执行终止时间戳
     * @param futureSupplier
     * @param <T>
     * @return
     */
    public static <T> Mono<Optional<T>> optionalFrom(long endTimeStamp, Supplier<Future<T>> futureSupplier) {
        long timeout = endTimeStamp - System.currentTimeMillis();
        if (timeout <= 0) {
            return Mono.just(Optional.empty());
        }
        InvokerHelper.setNcTimeout((int) timeout); // 动态超时
        return Mono.create(sink -> Futures.addCallback(futureSupplier.get(),
                new FutureCallback<T>() {
                    @Override
                    public void onSuccess(T result) {
                        sink.success(Optional.ofNullable(result));
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        log.warn(t.getMessage());
                        sink.success(Optional.empty()); // 超时返回empty
                    }
                }));
    }
}
