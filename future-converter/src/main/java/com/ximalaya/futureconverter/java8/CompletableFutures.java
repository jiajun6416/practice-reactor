package com.ximalaya.futureconverter.java8;

import com.ximalaya.futureconverter.Consumers.*;
import com.ximalaya.futureconverter.Functions.*;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * @author jiajun
 */
public class CompletableFutures {

    public static <R1, U> CompletableFuture<U> combine(CompletionStage<? extends R1> s1,
                                                       Function1<? super R1, ? extends U> fn) {
        return reduceCombine(fn, s1);
    }

    public static <R1, R2, U> CompletableFuture<U> combine(CompletionStage<? extends R1> s1,
                                                           CompletionStage<? extends R2> s2,
                                                           Function2<? super R1, ? super R2, ? extends U> fn) {
        return reduceCombine(fn, s1, s2);
    }

    public static <R1, R2, R3, U> CompletableFuture<U> combine(CompletionStage<? extends R1> s1,
                                                               CompletionStage<? extends R2> s2,
                                                               CompletionStage<? extends R3> s3,
                                                               Function3<? super R1, ? super R2, ? super R3, ? extends U> fn) {
        return reduceCombine(fn, s1, s2, s3);
    }

    public static <R1, R2, R3, R4, U> CompletableFuture<U> combine(CompletionStage<? extends R1> s1,
                                                                   CompletionStage<? extends R2> s2,
                                                                   CompletionStage<? extends R3> s3,
                                                                   CompletionStage<? extends R4> s4,
                                                                   Function4<? super R1, ? super R2, ? super R3, ? super R4, ? extends U> fn) {
        return reduceCombine(fn, s1, s2, s3, s4);
    }

    public static <R1, R2, R3, R4, R5, U> CompletableFuture<U> combine(CompletionStage<? extends R1> s1,
                                                                       CompletionStage<? extends R2> s2,
                                                                       CompletionStage<? extends R3> s3,
                                                                       CompletionStage<? extends R4> s4,
                                                                       CompletionStage<? extends R5> s5,
                                                                       Function5<? super R1, ? super R2, ? super R3, ? super R4, ? super R5, ? extends U> fn) {
        return reduceCombine(fn, s1, s2, s3, s4, s5);
    }

    public static <R1, R2, R3, R4, R5, R6, U> CompletableFuture<U> combine(CompletionStage<? extends R1> s1,
                                                                           CompletionStage<? extends R2> s2,
                                                                           CompletionStage<? extends R3> s3,
                                                                           CompletionStage<? extends R4> s4,
                                                                           CompletionStage<? extends R5> s5,
                                                                           CompletionStage<? extends R6> s6,
                                                                           Function6<? super R1, ? super R2, ? super R3, ? super R4, ? super R5, ? super R6, ? extends U> fn) {
        return reduceCombine(fn, s1, s2, s3, s4, s5, s6);
    }

    public static <R1, R2, R3, R4, R5, R6, R7, U> CompletableFuture<U> combine(CompletionStage<? extends R1> s1,
                                                                               CompletionStage<? extends R2> s2,
                                                                               CompletionStage<? extends R3> s3,
                                                                               CompletionStage<? extends R4> s4,
                                                                               CompletionStage<? extends R5> s5,
                                                                               CompletionStage<? extends R6> s6,
                                                                               CompletionStage<? extends R7> s7,
                                                                               Function7<? super R1, ? super R2, ? super R3, ? super R4, ? super R5, ? super R6, ? super R7, ? extends U> fn) {
        return reduceCombine(fn, s1, s2, s3, s4, s5, s6, s7);
    }

    public static <R1, R2, R3, R4, R5, R6, R7, R8, U> CompletableFuture<U> combine(CompletionStage<? extends R1> s1,
                                                                                   CompletionStage<? extends R2> s2,
                                                                                   CompletionStage<? extends R3> s3,
                                                                                   CompletionStage<? extends R4> s4,
                                                                                   CompletionStage<? extends R5> s5,
                                                                                   CompletionStage<? extends R6> s6,
                                                                                   CompletionStage<? extends R7> s7,
                                                                                   CompletionStage<? extends R8> s8,
                                                                                   Function8<? super R1, ? super R2, ? super R3, ? super R4, ? super R5, ? super R6, ? super R7, ? super R8, ? extends U> fn) {
        return reduceCombine(fn, s1, s2, s3, s4, s5, s6, s7, s8);
    }

    public static <R1, R2, R3, R4, R5, R6, R7, R8, R9, U> CompletableFuture<U> combine(CompletionStage<? extends R1> s1,
                                                                                       CompletionStage<? extends R2> s2,
                                                                                       CompletionStage<? extends R3> s3,
                                                                                       CompletionStage<? extends R4> s4,
                                                                                       CompletionStage<? extends R5> s5,
                                                                                       CompletionStage<? extends R6> s6,
                                                                                       CompletionStage<? extends R7> s7,
                                                                                       CompletionStage<? extends R8> s8,
                                                                                       CompletionStage<? extends R9> s9,
                                                                                       Function9<? super R1, ? super R2, ? super R3, ? super R4, ? super R5, ? super R6, ? super R7, ? super R8, ? super R9, ? extends U> fn) {
        return reduceCombine(fn, s1, s2, s3, s4, s5, s6, s7, s8, s9);
    }

    public static <R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, U> CompletableFuture<U> combine(CompletionStage<? extends R1> s1,
                                                                                            CompletionStage<? extends R2> s2,
                                                                                            CompletionStage<? extends R3> s3,
                                                                                            CompletionStage<? extends R4> s4,
                                                                                            CompletionStage<? extends R5> s5,
                                                                                            CompletionStage<? extends R6> s6,
                                                                                            CompletionStage<? extends R7> s7,
                                                                                            CompletionStage<? extends R8> s8,
                                                                                            CompletionStage<? extends R9> s9,
                                                                                            CompletionStage<? extends R10> s10,
                                                                                            Function10<? super R1, ? super R2, ? super R3, ? super R4, ? super R5, ? super R6, ? super R7, ? super R8, ? super R9, ? super R10, ? extends U> fn) {
        return reduceCombine(fn, s1, s2, s3, s4, s5, s6, s7, s8, s9, s10);
    }

    public static <R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, U> CompletableFuture<U> combine(CompletionStage<? extends R1> s1,
                                                                                                 CompletionStage<? extends R2> s2,
                                                                                                 CompletionStage<? extends R3> s3,
                                                                                                 CompletionStage<? extends R4> s4,
                                                                                                 CompletionStage<? extends R5> s5,
                                                                                                 CompletionStage<? extends R6> s6,
                                                                                                 CompletionStage<? extends R7> s7,
                                                                                                 CompletionStage<? extends R8> s8,
                                                                                                 CompletionStage<? extends R9> s9,
                                                                                                 CompletionStage<? extends R10> s10,
                                                                                                 CompletionStage<? extends R11> s11,
                                                                                                 Function11<? super R1, ? super R2, ? super R3, ? super R4, ? super R5, ? super R6, ? super R7, ? super R8, ? super R9, ? super R10, ? super R11, ? extends U> fn) {
        return reduceCombine(fn, s1, s2, s3, s4, s5, s6, s7, s8, s9, s10, s11);
    }

    public static <R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, U> CompletableFuture<U> combine(CompletionStage<? extends R1> s1,
                                                                                                      CompletionStage<? extends R2> s2,
                                                                                                      CompletionStage<? extends R3> s3,
                                                                                                      CompletionStage<? extends R4> s4,
                                                                                                      CompletionStage<? extends R5> s5,
                                                                                                      CompletionStage<? extends R6> s6,
                                                                                                      CompletionStage<? extends R7> s7,
                                                                                                      CompletionStage<? extends R8> s8,
                                                                                                      CompletionStage<? extends R9> s9,
                                                                                                      CompletionStage<? extends R10> s10,
                                                                                                      CompletionStage<? extends R11> s11,
                                                                                                      CompletionStage<? extends R12> s12,
                                                                                                      Function12<? super R1, ? super R2, ? super R3, ? super R4, ? super R5, ? super R6, ? super R7, ? super R8, ? super R9, ? super R10, ? super R11, ? super R12, ? extends U> fn) {
        return reduceCombine(fn, s1, s2, s3, s4, s5, s6, s7, s8, s9, s10, s11, s12);
    }

    public static <R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13, U> CompletableFuture<U> combine(CompletionStage<? extends R1> s1,
                                                                                                           CompletionStage<? extends R2> s2,
                                                                                                           CompletionStage<? extends R3> s3,
                                                                                                           CompletionStage<? extends R4> s4,
                                                                                                           CompletionStage<? extends R5> s5,
                                                                                                           CompletionStage<? extends R6> s6,
                                                                                                           CompletionStage<? extends R7> s7,
                                                                                                           CompletionStage<? extends R8> s8,
                                                                                                           CompletionStage<? extends R9> s9,
                                                                                                           CompletionStage<? extends R10> s10,
                                                                                                           CompletionStage<? extends R11> s11,
                                                                                                           CompletionStage<? extends R12> s12,
                                                                                                           CompletionStage<? extends R13> s13,
                                                                                                           Function13<? super R1, ? super R2, ? super R3, ? super R4, ? super R5, ? super R6, ? super R7, ? super R8, ? super R9, ? super R10, ? super R11, ? super R12, ? super R13, ? extends U> fn) {
        return reduceCombine(fn, s1, s2, s3, s4, s5, s6, s7, s8, s9, s10, s11, s12, s13);
    }

    public static <R1> CompletableFuture<Void> acceptBoth(CompletionStage<? extends R1> s1,
                                                          Consumer1<? super R1> fn) {
        return reduceConsumer(fn, s1);
    }

    public static <R1, R2> CompletableFuture<Void> acceptBoth(CompletionStage<? extends R1> s1,
                                                              CompletionStage<? extends R2> s2,
                                                              Consumer2<? super R1, ? super R2> fn) {
        return reduceConsumer(fn, s1, s2);
    }

    public static <R1, R2, R3> CompletableFuture<Void> acceptBoth(CompletionStage<? extends R1> s1,
                                                                  CompletionStage<? extends R2> s2,
                                                                  CompletionStage<? extends R3> s3,
                                                                  Consumer3<? super R1, ? super R2, ? super R3> fn) {
        return reduceConsumer(fn, s1, s2, s3);
    }

    public static <R1, R2, R3, R4> CompletableFuture<Void> acceptBoth(CompletionStage<? extends R1> s1,
                                                                      CompletionStage<? extends R2> s2,
                                                                      CompletionStage<? extends R3> s3,
                                                                      CompletionStage<? extends R4> s4,
                                                                      Consumer4<? super R1, ? super R2, ? super R3, ? super R4> fn) {
        return reduceConsumer(fn, s1, s2, s3, s4);
    }

    public static <R1, R2, R3, R4, R5> CompletableFuture<Void> acceptBoth(CompletionStage<? extends R1> s1,
                                                                          CompletionStage<? extends R2> s2,
                                                                          CompletionStage<? extends R3> s3,
                                                                          CompletionStage<? extends R4> s4,
                                                                          CompletionStage<? extends R5> s5,
                                                                          Consumer5<? super R1, ? super R2, ? super R3, ? super R4, ? super R5> fn) {
        return reduceConsumer(fn, s1, s2, s3, s4, s5);
    }

    public static <R1, R2, R3, R4, R5, R6> CompletableFuture<Void> acceptBoth(CompletionStage<? extends R1> s1,
                                                                              CompletionStage<? extends R2> s2,
                                                                              CompletionStage<? extends R3> s3,
                                                                              CompletionStage<? extends R4> s4,
                                                                              CompletionStage<? extends R5> s5,
                                                                              CompletionStage<? extends R6> s6,
                                                                              Consumer6<? super R1, ? super R2, ? super R3, ? super R4, ? super R5, ? super R6> fn) {
        return reduceConsumer(fn, s1, s2, s3, s4, s5, s6);
    }

    public static <R1, R2, R3, R4, R5, R6, R7> CompletableFuture<Void> acceptBoth(CompletionStage<? extends R1> s1,
                                                                                  CompletionStage<? extends R2> s2,
                                                                                  CompletionStage<? extends R3> s3,
                                                                                  CompletionStage<? extends R4> s4,
                                                                                  CompletionStage<? extends R5> s5,
                                                                                  CompletionStage<? extends R6> s6,
                                                                                  CompletionStage<? extends R7> s7,
                                                                                  Consumer7<? super R1, ? super R2, ? super R3, ? super R4, ? super R5, ? super R6, ? super R7> fn) {
        return reduceConsumer(fn, s1, s2, s3, s4, s5, s6, s7);
    }

    public static <R1, R2, R3, R4, R5, R6, R7, R8> CompletableFuture<Void> acceptBoth(CompletionStage<? extends R1> s1,
                                                                                      CompletionStage<? extends R2> s2,
                                                                                      CompletionStage<? extends R3> s3,
                                                                                      CompletionStage<? extends R4> s4,
                                                                                      CompletionStage<? extends R5> s5,
                                                                                      CompletionStage<? extends R6> s6,
                                                                                      CompletionStage<? extends R7> s7,
                                                                                      CompletionStage<? extends R8> s8,
                                                                                      Consumer8<? super R1, ? super R2, ? super R3, ? super R4, ? super R5, ? super R6, ? super R7, ? super R8> fn) {
        return reduceConsumer(fn, s1, s2, s3, s4, s5, s6, s7, s8);
    }

    public static <R1, R2, R3, R4, R5, R6, R7, R8, R9> CompletableFuture<Void> acceptBoth(CompletionStage<? extends R1> s1,
                                                                                          CompletionStage<? extends R2> s2,
                                                                                          CompletionStage<? extends R3> s3,
                                                                                          CompletionStage<? extends R4> s4,
                                                                                          CompletionStage<? extends R5> s5,
                                                                                          CompletionStage<? extends R6> s6,
                                                                                          CompletionStage<? extends R7> s7,
                                                                                          CompletionStage<? extends R8> s8,
                                                                                          CompletionStage<? extends R9> s9,
                                                                                          Consumer9<? super R1, ? super R2, ? super R3, ? super R4, ? super R5, ? super R6, ? super R7, ? super R8, ? super R9> fn) {
        return reduceConsumer(fn, s1, s2, s3, s4, s5, s6, s7, s8, s9);
    }

    public static <R1, R2, R3, R4, R5, R6, R7, R8, R9, R10> CompletableFuture<Void> acceptBoth(CompletionStage<? extends R1> s1,
                                                                                               CompletionStage<? extends R2> s2,
                                                                                               CompletionStage<? extends R3> s3,
                                                                                               CompletionStage<? extends R4> s4,
                                                                                               CompletionStage<? extends R5> s5,
                                                                                               CompletionStage<? extends R6> s6,
                                                                                               CompletionStage<? extends R7> s7,
                                                                                               CompletionStage<? extends R8> s8,
                                                                                               CompletionStage<? extends R9> s9,
                                                                                               CompletionStage<? extends R10> s10,
                                                                                               Consumer10<? super R1, ? super R2, ? super R3, ? super R4, ? super R5, ? super R6, ? super R7, ? super R8, ? super R9, ? super R10> fn) {
        return reduceConsumer(fn, s1, s2, s3, s4, s5, s6, s7, s8, s9, s10);
    }

    public static <R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11> CompletableFuture<Void> acceptBoth(CompletionStage<? extends R1> s1,
                                                                                                    CompletionStage<? extends R2> s2,
                                                                                                    CompletionStage<? extends R3> s3,
                                                                                                    CompletionStage<? extends R4> s4,
                                                                                                    CompletionStage<? extends R5> s5,
                                                                                                    CompletionStage<? extends R6> s6,
                                                                                                    CompletionStage<? extends R7> s7,
                                                                                                    CompletionStage<? extends R8> s8,
                                                                                                    CompletionStage<? extends R9> s9,
                                                                                                    CompletionStage<? extends R10> s10,
                                                                                                    CompletionStage<? extends R11> s11,
                                                                                                    Consumer11<? super R1, ? super R2, ? super R3, ? super R4, ? super R5, ? super R6, ? super R7, ? super R8, ? super R9, ? super R10, ? super R11> fn) {
        return reduceConsumer(fn, s1, s2, s3, s4, s5, s6, s7, s8, s9, s10, s11);
    }

    public static <R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12> CompletableFuture<Void> acceptBoth(CompletionStage<? extends R1> s1,
                                                                                                         CompletionStage<? extends R2> s2,
                                                                                                         CompletionStage<? extends R3> s3,
                                                                                                         CompletionStage<? extends R4> s4,
                                                                                                         CompletionStage<? extends R5> s5,
                                                                                                         CompletionStage<? extends R6> s6,
                                                                                                         CompletionStage<? extends R7> s7,
                                                                                                         CompletionStage<? extends R8> s8,
                                                                                                         CompletionStage<? extends R9> s9,
                                                                                                         CompletionStage<? extends R10> s10,
                                                                                                         CompletionStage<? extends R11> s11,
                                                                                                         CompletionStage<? extends R12> s12,
                                                                                                         Consumer12<? super R1, ? super R2, ? super R3, ? super R4, ? super R5, ? super R6, ? super R7, ? super R8, ? super R9, ? super R10, ? super R11, ? super R12> fn) {
        return reduceConsumer(fn, s1, s2, s3, s4, s5, s6, s7, s8, s9, s10, s11, s12);
    }

    public static <R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13> CompletableFuture<Void> acceptBoth(CompletionStage<? extends R1> s1,
                                                                                                              CompletionStage<? extends R2> s2,
                                                                                                              CompletionStage<? extends R3> s3,
                                                                                                              CompletionStage<? extends R4> s4,
                                                                                                              CompletionStage<? extends R5> s5,
                                                                                                              CompletionStage<? extends R6> s6,
                                                                                                              CompletionStage<? extends R7> s7,
                                                                                                              CompletionStage<? extends R8> s8,
                                                                                                              CompletionStage<? extends R9> s9,
                                                                                                              CompletionStage<? extends R10> s10,
                                                                                                              CompletionStage<? extends R11> s11,
                                                                                                              CompletionStage<? extends R12> s12,
                                                                                                              CompletionStage<? extends R13> s13,
                                                                                                              Consumer13<? super R1, ? super R2, ? super R3, ? super R4, ? super R5, ? super R6, ? super R7, ? super R8, ? super R9, ? super R10, ? super R11, ? super R12, ? super R13> fn) {
        return reduceConsumer(fn, s1, s2, s3, s4, s5, s6, s7, s8, s9, s10, s11, s12, s13);
    }

    public static CompletableFuture<Void> allOf(CompletableFuture... cfs) {
        if (cfs == null) {
            return CompletableFuture.completedFuture(null);
        }
        CompletableFuture[] nonNullFutures = Arrays.stream(cfs).filter(Objects::nonNull).toArray(CompletableFuture[]::new);
        if (nonNullFutures.length == 0) {
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.allOf(nonNullFutures);
    }

    public static <R1, U> CompletableFuture<U> compose(CompletionStage<? extends R1> s1,
                                                       Function1<? super R1, ? extends CompletionStage<U>> f1) {

        return Optional.ofNullable(s1).orElse(CompletableFuture.completedFuture(null)).thenCompose(f1::apply).toCompletableFuture();
    }

    public static <R1, R2, U> CompletableFuture<U> compose(CompletionStage<? extends R1> s1,
                                                           Function1<? super R1, ? extends CompletionStage<R2>> f1,
                                                           Function2<? super R1, ? super R2, ? extends CompletionStage<U>> f2) {

        Object[] result = new Object[1];
        return Optional.ofNullable(s1).orElse(CompletableFuture.completedFuture(null))
                .thenCompose(r1 -> {
                    result[0] = r1;
                    CompletionStage<R2> s2 = f1.apply(r1);
                    return Optional.ofNullable(s2).orElse(CompletableFuture.completedFuture(null));
                }).thenCompose(r2 -> f2.apply((R1) result[0], r2)).toCompletableFuture();
    }

    public static <R1, R2, R3, U> CompletableFuture<U> compose(CompletionStage<? extends R1> s1,
                                                               Function1<? super R1, ? extends CompletionStage<R2>> f1,
                                                               Function2<? super R1, ? super R2, ? extends CompletionStage<R3>> f2,
                                                               Function3<? super R1, ? super R2, ? super R3, ? extends CompletionStage<U>> f3
    ) {
        Object[] result = new Object[2];
        return Optional.ofNullable(s1).orElse(CompletableFuture.completedFuture(null))
                .thenCompose(r1 -> {
                    result[0] = r1;
                    CompletionStage<R2> s2 = f1.apply(r1);
                    return Optional.ofNullable(s2).orElse(CompletableFuture.completedFuture(null));
                }).thenCompose(r2 -> {
                    result[1] = r2;
                    CompletionStage<R3> s3 = f2.apply((R1) result[0], r2);
                    return Optional.ofNullable(s3).orElse(CompletableFuture.completedFuture(null));
                }).thenCompose(r3 -> f3.apply((R1) result[0], (R2) result[1], r3)).toCompletableFuture();
    }

    public static <R1, R2, R3, R4, U> CompletableFuture<U> compose(CompletionStage<? extends R1> s1,
                                                                   Function1<? super R1, ? extends CompletionStage<R2>> f1,
                                                                   Function2<? super R1, ? super R2, ? extends CompletionStage<R3>> f2,
                                                                   Function3<? super R1, ? super R2, ? super R3, ? extends CompletionStage<R4>> f3,
                                                                   Function4<? super R1, ? super R2, ? super R3, ? super R4, ? extends CompletionStage<U>> f4
    ) {
        Object[] result = new Object[3];
        return Optional.ofNullable(s1).orElse(CompletableFuture.completedFuture(null))
                .thenCompose(r1 -> {
                    result[0] = r1;
                    CompletionStage<R2> s2 = f1.apply(r1);
                    return Optional.ofNullable(s2).orElse(CompletableFuture.completedFuture(null));
                }).thenCompose(r2 -> {
                    result[1] = r2;
                    CompletionStage<R3> s3 = f2.apply((R1) result[0], r2);
                    return Optional.ofNullable(s3).orElse(CompletableFuture.completedFuture(null));
                }).thenCompose(r3 -> {
                    result[2] = r3;
                    CompletionStage<R4> s4 = f3.apply((R1) result[0], (R2) result[1], r3);
                    return Optional.ofNullable(s4).orElse(CompletableFuture.completedFuture(null));
                }).thenCompose(r4 -> f4.apply((R1) result[0], (R2) result[1], (R3) result[2], r4)).toCompletableFuture();
    }

    public static <R1, R2, R3, R4, R5, U> CompletableFuture<U> compose(CompletionStage<? extends R1> s1,
                                                                       Function1<? super R1, ? extends CompletionStage<R2>> f1,
                                                                       Function2<? super R1, ? super R2, ? extends CompletionStage<R3>> f2,
                                                                       Function3<? super R1, ? super R2, ? super R3, ? extends CompletionStage<R4>> f3,
                                                                       Function4<? super R1, ? super R2, ? super R3, ? super R4, ? extends CompletionStage<R5>> f4,
                                                                       Function5<? super R1, ? super R2, ? super R3, ? super R4, ? super R5, ? extends CompletionStage<U>> f5
    ) {
        Object[] result = new Object[4];
        return Optional.ofNullable(s1).orElse(CompletableFuture.completedFuture(null))
                .thenCompose(r1 -> {
                    result[0] = r1;
                    CompletionStage<R2> s2 = f1.apply(r1);
                    return Optional.ofNullable(s2).orElse(CompletableFuture.completedFuture(null));
                }).thenCompose(r2 -> {
                    result[1] = r2;
                    CompletionStage<R3> s3 = f2.apply((R1) result[0], r2);
                    return Optional.ofNullable(s3).orElse(CompletableFuture.completedFuture(null));
                }).thenCompose(r3 -> {
                    result[2] = r3;
                    CompletionStage<R4> s4 = f3.apply((R1) result[0], (R2) result[1], r3);
                    return Optional.ofNullable(s4).orElse(CompletableFuture.completedFuture(null));
                }).thenCompose(r4 -> {
                    result[3] = r4;
                    CompletionStage<R5> s5 = f4.apply((R1) result[0], (R2) result[1], (R3) result[2], r4);
                    return Optional.ofNullable(s5).orElse(CompletableFuture.completedFuture(null));
                }).thenCompose(r5 -> f5.apply((R1) result[0], (R2) result[1], (R3) result[2], (R4) result[3], r5)).toCompletableFuture();
    }

    private static <F> CompletableFuture reduceCombine(F function, CompletionStage... completionStages) {
        int size = completionStages.length;
        Object[] allResult = new Object[size];
        CompletableFuture<Object[]> resultFuture = CompletableFuture.completedFuture(allResult);
        for (int i = 0; i < size; i++) {
            final int j = i;
            CompletionStage completionStage = completionStages[i];
            if (completionStage == null) {
                allResult[j] = null;
            } else {
                resultFuture = resultFuture.thenCombine(completionStage,
                        (results, result) -> {
                            results[j] = result;
                            return results;
                        });
            }
        }
        switch (size) {
            case 1:
                return resultFuture.thenApply(((Function1) function)::apply_);
            case 2:
                return resultFuture.thenApply(((Function2) function)::apply_);
            case 3:
                return resultFuture.thenApply(((Function3) function)::apply_);
            case 4:
                return resultFuture.thenApply(((Function4) function)::apply_);
            case 5:
                return resultFuture.thenApply(((Function5) function)::apply_);
            case 6:
                return resultFuture.thenApply(((Function6) function)::apply_);
            case 7:
                return resultFuture.thenApply(((Function7) function)::apply_);
            case 8:
                return resultFuture.thenApply(((Function8) function)::apply_);
            case 9:
                return resultFuture.thenApply(((Function9) function)::apply_);
            case 10:
                return resultFuture.thenApply(((Function10) function)::apply_);
            case 11:
                return resultFuture.thenApply(((Function11) function)::apply_);
            case 12:
                return resultFuture.thenApply(((Function12) function)::apply_);
            case 13:
                return resultFuture.thenApply(((Function13) function)::apply_);
            default:
                throw new IllegalStateException("only support combine completionStages max size is 13!");
        }
    }

    private static <C> CompletableFuture<Void> reduceConsumer(C consumer, CompletionStage... completionStages) {
        int size = completionStages.length;
        Object[] allResult = new Object[size];
        CompletableFuture<Object[]> resultFuture = CompletableFuture.completedFuture(allResult);
        for (int i = 0; i < size; i++) {
            final int j = i;
            CompletionStage completionStage = completionStages[i];
            if (completionStage == null) {
                allResult[j] = null;
            } else {
                resultFuture = resultFuture.thenCombine(completionStage,
                        (results, result) -> {
                            results[j] = result;
                            return results;
                        });
            }
        }
        switch (size) {
            case 1:
                return resultFuture.thenAccept(((Consumer1) consumer)::accept_);
            case 2:
                return resultFuture.thenAccept(((Consumer2) consumer)::accept_);
            case 3:
                return resultFuture.thenAccept(((Consumer3) consumer)::accept_);
            case 4:
                return resultFuture.thenAccept(((Consumer4) consumer)::accept_);
            case 5:
                return resultFuture.thenAccept(((Consumer5) consumer)::accept_);
            case 6:
                return resultFuture.thenAccept(((Consumer6) consumer)::accept_);
            case 7:
                return resultFuture.thenAccept(((Consumer7) consumer)::accept_);
            case 8:
                return resultFuture.thenAccept(((Consumer8) consumer)::accept_);
            case 9:
                return resultFuture.thenAccept(((Consumer9) consumer)::accept_);
            case 10:
                return resultFuture.thenAccept(((Consumer10) consumer)::accept_);
            case 11:
                return resultFuture.thenAccept(((Consumer11) consumer)::accept_);
            case 12:
                return resultFuture.thenAccept(((Consumer12) consumer)::accept_);
            case 13:
                return resultFuture.thenAccept(((Consumer13) consumer)::accept_);
            default:
                throw new IllegalStateException("only support accept completionStages max size is 13!");
        }
    }
}
