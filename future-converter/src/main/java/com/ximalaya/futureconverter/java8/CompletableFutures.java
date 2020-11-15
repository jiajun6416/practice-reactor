package com.ximalaya.futureconverter.java8;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * @author jiajun
 */
@Slf4j
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
                                                                                       CompletionStage<? extends R8> s9,
                                                                                       Function9<? super R1, ? super R2, ? super R3, ? super R4, ? super R5, ? super R6, ? super R7, ? super R8, ? super R9, ? extends U> fn) {
        return reduceCombine(fn, s1, s2, s3, s4, s5, s6, s7, s8, s9);
    }

    @FunctionalInterface
    interface Function1<R1, U> {
        U apply(R1 r1);

        default U apply(Object[] objects) {
            return apply((R1) objects[0]);
        }
    }

    @FunctionalInterface
    interface Function2<R1, R2, U> {
        U apply(R1 r1, R2 r2);

        default U apply(Object[] objects) {
            return apply((R1) objects[0], (R2) objects[1]);
        }
    }

    @FunctionalInterface
    interface Function3<R1, R2, R3, U> {
        U apply(R1 r1, R2 r2, R3 r3);

        default U apply(Object[] objects) {
            return apply((R1) objects[0], (R2) objects[1], (R3) objects[2]);
        }
    }

    @FunctionalInterface
    interface Function4<R1, R2, R3, R4, U> {
        U apply(R1 r1, R2 r2, R3 r3, R4 r4);

        default U apply(Object[] objects) {
            return apply((R1) objects[0], (R2) objects[1], (R3) objects[2], (R4) objects[3]);
        }
    }

    @FunctionalInterface
    interface Function5<R1, R2, R3, R4, R5, U> {
        U apply(R1 r1, R2 r2, R3 r3, R4 r4, R5 r5);

        default U apply(Object[] objects) {
            return apply((R1) objects[0], (R2) objects[1], (R3) objects[2], (R4) objects[3], (R5) objects[4]);
        }
    }

    @FunctionalInterface
    interface Function6<R1, R2, R3, R4, R5, R6, U> {
        U apply(R1 r1, R2 r2, R3 r3, R4 r4, R5 r5, R6 r6);

        default U apply(Object[] objects) {
            return apply((R1) objects[0], (R2) objects[1], (R3) objects[2], (R4) objects[3], (R5) objects[4], (R6) objects[5]);
        }
    }

    @FunctionalInterface
    interface Function7<R1, R2, R3, R4, R5, R6, R7, U> {
        U apply(R1 r1, R2 r2, R3 r3, R4 r4, R5 r5, R6 r6, R7 r7);

        default U apply(Object[] objects) {
            return apply((R1) objects[0], (R2) objects[1], (R3) objects[2], (R4) objects[3], (R5) objects[4], (R6) objects[5], (R7) objects[6]);
        }
    }

    @FunctionalInterface
    interface Function8<R1, R2, R3, R4, R5, R6, R7, R8, U> {
        U apply(R1 r1, R2 r2, R3 r3, R4 r4, R5 r5, R6 r6, R7 r7, R8 r8);

        default U apply(Object[] objects) {
            return apply((R1) objects[0], (R2) objects[1], (R3) objects[2], (R4) objects[3], (R5) objects[4], (R6) objects[5], (R7) objects[6], (R8) objects[7]);
        }
    }

    @FunctionalInterface
    interface Function9<R1, R2, R3, R4, R5, R6, R7, R8, R9, U> {
        U apply(R1 r1, R2 r2, R3 r3, R4 r4, R5 r5, R6 r6, R7 r7, R8 r8, R9 r9);

        default U apply(Object[] objects) {
            return apply((R1) objects[0], (R2) objects[1], (R3) objects[2], (R4) objects[3], (R5) objects[4], (R6) objects[5], (R7) objects[6], (R8) objects[7], (R9) objects[8]);
        }
    }

    private static <F> CompletableFuture reduceCombine(F function, CompletionStage... completionStages) {
        int size = completionStages.length;
        CompletableFuture<Object[]> resultFuture = CompletableFuture.completedFuture(new Object[size]);
        for (int i = 0; i < size; i++) {
            final int j = i;
            CompletionStage completionStage = completionStages[i];
            if (completionStage == null) {
                resultFuture.thenApply(results -> {
                    results[j] = null;
                    return results;
                });
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
                return resultFuture.thenApply(((Function1) function)::apply);
            case 2:
                return resultFuture.thenApply(((Function2) function)::apply);
            case 3:
                return resultFuture.thenApply(((Function3) function)::apply);
            case 4:
                return resultFuture.thenApply(((Function4) function)::apply);
            case 5:
                return resultFuture.thenApply(((Function5) function)::apply);
            case 6:
                return resultFuture.thenApply(((Function6) function)::apply);
            case 7:
                return resultFuture.thenApply(((Function7) function)::apply);
            case 8:
                return resultFuture.thenApply(((Function8) function)::apply);
            case 9:
                return resultFuture.thenApply(((Function9) function)::apply);
            default:
                throw new IllegalStateException("only support combine completionStages max size is 9!");
        }
    }

    public static void main(String[] args) {
        combine(CompletableFuture.completedFuture(1), r1 -> {
            System.out.println("r1: " + r1);
            return r1;
        }).join();

        combine(CompletableFuture.completedFuture(1), null, (r1, r2) -> {
            System.out.println("r1: " + r1);
            System.out.println("r2: " + r2);
            return r1;
        }).join();

        combine(null, null, null, null, null, CompletableFuture.completedFuture(1), CompletableFuture.completedFuture(1), null, null,
                (r1, r2, r3, r4, r5, r6, r7, r8, r9) -> {
                    System.out.println("r1: " + r1);
                    System.out.println("r2: " + r2);
                    System.out.println("r3: " + r3);
                    System.out.println("r4: " + r4);
                    System.out.println("r5: " + r5);
                    System.out.println("r6: " + r6);
                    System.out.println("r7: " + r7);
                    System.out.println("r8: " + r8);
                    System.out.println("r9: " + r9);
                    return 0;
                }).join();
    }
}
