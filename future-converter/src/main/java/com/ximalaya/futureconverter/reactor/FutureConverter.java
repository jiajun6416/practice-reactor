package com.ximalaya.futureconverter.reactor;

import com.ximalaya.mainstay.common.InvokerHelper;
import com.ximalaya.mainstay.common.MainstayTimeoutException;
import com.ximalaya.mainstay.common.concurrent.Future;
import com.ximalaya.mainstay.common.concurrent.FutureCallback;
import com.ximalaya.mainstay.common.concurrent.Futures;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Mono中元素不能为null, 所以降级时候返回Optional
 *
 * @author jiajun
 */
public class FutureConverter {

    public static <T> Mono<T> toMono(Future<T> future, Function<Throwable, Throwable> function) {
        return Mono.create(sink -> Futures.addCallback(future,
                new FutureCallback<T>() {
                    @Override
                    public void onSuccess(T result) {
                        sink.success(result);
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        if (t instanceof MainstayTimeoutException) {
                            Schedulers.parallel().schedule(() -> sink.error(function.apply(t)));
                        } else {
                            sink.error(function.apply(t));
                        }
                    }
                }));
    }

    public static <T> Mono<Optional<T>> toMonoOptional(Future<T> future, Function<Throwable, Optional<T>> fallbackHandler) {
        return Mono.create(sink -> Futures.addCallback(future,
                new FutureCallback<T>() {
                    @Override
                    public void onSuccess(T result) {
                        sink.success(Optional.ofNullable(result));
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        if (t instanceof MainstayTimeoutException) {
                            Schedulers.parallel().schedule(() -> sink.success(fallbackHandler.apply(t)));
                        } else {
                            sink.success(fallbackHandler.apply(t));
                        }
                    }
                }));
    }

    public static <T> Mono<T> toMono(Supplier<Future<T>> futureSupplier, Long endTime, Function<Throwable, Throwable> exHandler) {
        long timeout = endTime - System.currentTimeMillis();
        if (timeout <= 0) {
            Exception timeoutException = new MainstayTimeoutException(" timeout [" + timeout + "] less then 0");
            return Mono.error(exHandler.apply(timeoutException));
        }
        InvokerHelper.setNcTimeout((int) timeout);
        return Mono.create(sink -> Futures.addCallback(futureSupplier.get(),
                new FutureCallback<T>() {
                    @Override
                    public void onSuccess(T result) {
                        sink.success(result);
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        if (t instanceof MainstayTimeoutException) {
                            Schedulers.parallel().schedule(() -> sink.error(exHandler.apply(t)));
                        } else {
                            sink.error(exHandler.apply(t));
                        }
                    }
                }));
    }

    public static <T> Mono<Optional<T>> toMonoOptional(Supplier<Future<T>> futureSupplier, Long endTime, Function<Throwable, Optional<T>> fallbackFunction) {
        long timeout = endTime - System.currentTimeMillis();
        if (timeout <= 0) {
            Exception timeoutException = new MainstayTimeoutException(" timeout [" + timeout + "] less then 0");
            return Mono.just(fallbackFunction.apply(timeoutException));
        }
        InvokerHelper.setNcTimeout((int) timeout);
        return Mono.create(sink -> Futures.addCallback(futureSupplier.get(),
                new FutureCallback<T>() {
                    @Override
                    public void onSuccess(T result) {
                        sink.success(Optional.ofNullable(result));
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        if (t instanceof MainstayTimeoutException) {
                            Schedulers.parallel().schedule(() -> sink.success(fallbackFunction.apply(t)));
                        } else {
                            sink.success(fallbackFunction.apply(t));
                        }
                    }
                }));
    }
}