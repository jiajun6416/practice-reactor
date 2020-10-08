package com.jiajun.practice.reactor.webflux.util;

import com.ximalaya.mainstay.common.concurrent.Future;
import com.ximalaya.mainstay.common.concurrent.FutureCallback;
import com.ximalaya.mainstay.common.concurrent.Futures;
import com.ximalaya.mainstay.common.concurrent.SettableFuture;

/**
 * @author jiajun
 */
public class FutureUtil {

    /**
     * Reactor中Void会终止操作符, 需要转换下
     *
     * @param voidFuture
     * @return
     */
    public static <T> Future<T> voidMap(Future<Void> voidFuture, T t) {
        SettableFuture<T> future = Futures.newSettableFuture();
        Futures.addCallback(voidFuture, new FutureCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                future.set(t);
            }

            @Override
            public void onFailure(Throwable t) {
                future.setException(t);
            }
        });
        return future;
    }
}
