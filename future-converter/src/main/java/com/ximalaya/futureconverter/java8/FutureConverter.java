package com.ximalaya.futureconverter.java8;

import com.ximalaya.mainstay.common.InvokerHelper;
import com.ximalaya.mainstay.common.MainstayTimeoutException;
import com.ximalaya.mainstay.common.concurrent.Future;
import com.ximalaya.mainstay.common.concurrent.FutureCallback;
import com.ximalaya.mainstay.common.concurrent.Futures;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author jiajun
 */
public class FutureConverter {

    public static <T> CompletableFuture<T> toCompletableFuture(Future<T> future, Function<Throwable, Object> exceptionHandler) {
        CompletableFuture<T> completableFuture = new CompletableFuture<>();
        Futures.addCallback(future, new FutureCallback<T>() {
            @Override
            public void onSuccess(T result) {
                completableFuture.complete(result);
            }

            @Override
            public void onFailure(Throwable t) {
                Object value = null;
                try {
                    value = exceptionHandler.apply(t);
                } catch (Throwable e) {
                    value = e;
                }
                if (value != null && value instanceof Throwable) {
                    completableFuture.completeExceptionally((Throwable) value);
                } else {
                    completableFuture.complete((T) value);
                }
            }
        });
        return completableFuture;
    }

    public static <T> CompletableFuture<T> toCompletableFuture(Supplier<Future<T>> futureSupplier, long endTime, Function<Throwable, Object> exceptionHandler) {
        CompletableFuture<T> completableFuture = new CompletableFuture<>();
        long timeout = endTime - System.currentTimeMillis();
        if (timeout <= 0) {
            Exception t = new MainstayTimeoutException(" timeout [" + timeout + "] less then 0");
            Object value = null;
            try {
                value = exceptionHandler.apply(t);
            } catch (Throwable e) {
                value = e;
            }
            if (value != null && value instanceof Throwable) {
                completableFuture.completeExceptionally((Throwable) value);
            } else {
                completableFuture.complete((T) value);
            }
            return completableFuture;
        }
        InvokerHelper.setNcTimeout((int) timeout); // 动态超时
        Future<T> future = null;
        try {
            future = futureSupplier.get(); // 将非rpc异常转成future
        } catch (Throwable e) {
            future = Futures.immediateFailedFuture(e);
        }
        Futures.addCallback(future, new FutureCallback<T>() {
            @Override
            public void onSuccess(T result) {
                completableFuture.complete(result);
            }

            @Override
            public void onFailure(Throwable t) {
                Object value = null;
                try {
                    value = exceptionHandler.apply(t);
                } catch (Throwable e) {
                    value = e;
                }
                if (value != null && value instanceof Throwable) {
                    completableFuture.completeExceptionally((Throwable) value);
                } else {
                    completableFuture.complete((T) value);
                }
            }
        });
        return completableFuture;
    }
}
