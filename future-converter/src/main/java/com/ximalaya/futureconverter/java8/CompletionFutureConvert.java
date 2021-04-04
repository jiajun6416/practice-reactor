package com.ximalaya.futureconverter.java8;

import com.ximalaya.mainstay.common.concurrent.Future;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 对CompletableFuture#allOf的增强, CompletableFuture的stack上所有Completion都完成才完成
 *
 * @author jiajun
 */
public class CompletionFutureConvert {

    private List<CompletableFuture> rootCompletions = new LinkedList<>();

    private Thread thread = Thread.currentThread();

    /**
     * 所有根Future的所有依赖执行完成
     *
     * @return
     */
    public CompletableFuture<Void> allOf() {
        CompletableFuture[] allDependents = rootCompletions.stream().flatMap(it -> CompletionDependentUtil.dependentLeaf(it).stream()).toArray(CompletableFuture[]::new);
        return CompletableFuture.allOf(allDependents);
    }

    public <T> CompletableFuture<T> completionConvert(Future<T> future, Function<Throwable, Object> exceptionHandler) {
        CompletableFuture<T> completableFuture = FutureConverter.toCompletableFuture(future, exceptionHandler);
        addRootCompletionNode(completableFuture);
        return completableFuture;
    }

    public <T> CompletableFuture<T> completionConvert(Supplier<Future<T>> futureSupplier, long endTime, Function<Throwable, Object> exceptionHandler) {
        CompletableFuture<T> completableFuture = FutureConverter.toCompletableFuture(futureSupplier, endTime, exceptionHandler);
        addRootCompletionNode(completableFuture);
        return completableFuture;
    }

    private void addRootCompletionNode(CompletableFuture completableFuture) {
        if (Thread.currentThread() == thread) {
            rootCompletions.add(completableFuture);
        }
    }
}


