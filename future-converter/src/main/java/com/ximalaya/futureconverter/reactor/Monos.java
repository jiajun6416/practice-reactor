package com.ximalaya.futureconverter.reactor;

import com.ximalaya.futureconverter.Functions;
import com.ximalaya.futureconverter.Functions.Function3;
import com.ximalaya.futureconverter.Functions.Function4;
import reactor.core.publisher.Mono;

/**
 * @author jiajun
 */
public class Monos {

    public static <T1, T2, T3, U> Mono<U> zip(Mono<? extends T1> p1,
                                              Mono<? extends T2> p2,
                                              Mono<? extends T3> p3,
                                              Function3<T1, T2, T3, U> fun) {
        return Mono.zip(p1, p2, p3).map(tuple -> fun.apply(tuple.getT1(), tuple.getT2(), tuple.getT3()));
    }

    public static <T1, T2, T3, T4, U> Mono<U> zip(Mono<? extends T1> p1,
                                                  Mono<? extends T2> p2,
                                                  Mono<? extends T3> p3,
                                                  Mono<? extends T4> p4,
                                                  Function4<T1, T2, T3, T4, U> fun) {
        return Mono.zip(p1, p2, p3, p4).map(tuple -> fun.apply(tuple.getT1(), tuple.getT2(), tuple.getT3(), tuple.getT4()));
    }

    public static <T1, T2, T3, T4, T5, U> Mono<U> zip(Mono<? extends T1> p1,
                                                      Mono<? extends T2> p2,
                                                      Mono<? extends T3> p3,
                                                      Mono<? extends T4> p4,
                                                      Mono<? extends T5> p5,
                                                      Functions.Function5<T1, T2, T3, T4, T5, U> fun) {
        return Mono.zip(p1, p2, p3, p4, p5).map(tuple -> fun.apply(tuple.getT1(), tuple.getT2(), tuple.getT3(), tuple.getT4(), tuple.getT5()));
    }

    public static <T1, T2, T3, T4, T5, T6, U> Mono<U> zip(Mono<? extends T1> p1,
                                                          Mono<? extends T2> p2,
                                                          Mono<? extends T3> p3,
                                                          Mono<? extends T4> p4,
                                                          Mono<? extends T5> p5,
                                                          Mono<? extends T6> p6,
                                                          Functions.Function6<T1, T2, T3, T4, T5, T6, U> fun) {
        return Mono.zip(p1, p2, p3, p4, p5, p6).map(tuple -> fun.apply(tuple.getT1(), tuple.getT2(), tuple.getT3(), tuple.getT4(), tuple.getT5(), tuple.getT6()));
    }

    public static <T1, T2, T3, T4, T5, T6, T7, U> Mono<U> zip(Mono<? extends T1> p1,
                                                              Mono<? extends T2> p2,
                                                              Mono<? extends T3> p3,
                                                              Mono<? extends T4> p4,
                                                              Mono<? extends T5> p5,
                                                              Mono<? extends T6> p6,
                                                              Mono<? extends T7> p7,
                                                              Functions.Function7<T1, T2, T3, T4, T5, T6, T7, U> fun) {
        return Mono.zip(p1, p2, p3, p4, p5, p6, p7).map(tuple -> fun.apply(tuple.getT1(), tuple.getT2(), tuple.getT3(), tuple.getT4(), tuple.getT5(), tuple.getT6(), tuple.getT7()));
    }

    public static <T1, T2, T3, T4, T5, T6, T7, T8, U> Mono<U> zip(Mono<? extends T1> p1,
                                                                  Mono<? extends T2> p2,
                                                                  Mono<? extends T3> p3,
                                                                  Mono<? extends T4> p4,
                                                                  Mono<? extends T5> p5,
                                                                  Mono<? extends T6> p6,
                                                                  Mono<? extends T7> p7,
                                                                  Mono<? extends T8> p8,
                                                                  Functions.Function8<T1, T2, T3, T4, T5, T6, T7, T8, U> fun) {
        return Mono.zip(p1, p2, p3, p4, p5, p6, p7, p8).map(tuple -> fun.apply(tuple.getT1(), tuple.getT2(), tuple.getT3(), tuple.getT4(), tuple.getT5(), tuple.getT6(), tuple.getT7(), tuple.getT8()));
    }

    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, U> Mono<U> zip(Mono<? extends T1> p1,
                                                                      Mono<? extends T2> p2,
                                                                      Mono<? extends T3> p3,
                                                                      Mono<? extends T4> p4,
                                                                      Mono<? extends T5> p5,
                                                                      Mono<? extends T6> p6,
                                                                      Mono<? extends T7> p7,
                                                                      Mono<? extends T8> p8,
                                                                      Mono<? extends T9> p9,
                                                                      Functions.Function9<T1, T2, T3, T4, T5, T6, T7, T8, T9, U> fun) {
        return Mono.zip(objects -> fun.apply_(objects), p1, p2, p3, p4, p5, p6, p7, p8, p9);
    }

    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, U> Mono<U> zip(Mono<? extends T1> p1,
                                                                           Mono<? extends T2> p2,
                                                                           Mono<? extends T3> p3,
                                                                           Mono<? extends T4> p4,
                                                                           Mono<? extends T5> p5,
                                                                           Mono<? extends T6> p6,
                                                                           Mono<? extends T7> p7,
                                                                           Mono<? extends T8> p8,
                                                                           Mono<? extends T9> p9,
                                                                           Mono<? extends T10> p10,
                                                                           Functions.Function10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, U> fun) {
        return Mono.zip(objects -> fun.apply_(objects), p1, p2, p3, p4, p5, p6, p7, p8, p9, p10);
    }

    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, U> Mono<U> zip(Mono<? extends T1> p1,
                                                                                Mono<? extends T2> p2,
                                                                                Mono<? extends T3> p3,
                                                                                Mono<? extends T4> p4,
                                                                                Mono<? extends T5> p5,
                                                                                Mono<? extends T6> p6,
                                                                                Mono<? extends T7> p7,
                                                                                Mono<? extends T8> p8,
                                                                                Mono<? extends T9> p9,
                                                                                Mono<? extends T10> p10,
                                                                                Mono<? extends T11> p11,
                                                                                Functions.Function11<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, U> fun) {
        return Mono.zip(objects -> fun.apply_(objects), p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11);
    }

    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, U> Mono<U> zip(Mono<? extends T1> p1,
                                                                                     Mono<? extends T2> p2,
                                                                                     Mono<? extends T3> p3,
                                                                                     Mono<? extends T4> p4,
                                                                                     Mono<? extends T5> p5,
                                                                                     Mono<? extends T6> p6,
                                                                                     Mono<? extends T7> p7,
                                                                                     Mono<? extends T8> p8,
                                                                                     Mono<? extends T9> p9,
                                                                                     Mono<? extends T10> p10,
                                                                                     Mono<? extends T11> p11,
                                                                                     Mono<? extends T12> p12,
                                                                                     Functions.Function12<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, U> fun) {
        return Mono.zip(objects -> fun.apply_(objects), p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12);
    }

    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, U> Mono<U> zip(Mono<? extends T1> p1,
                                                                                          Mono<? extends T2> p2,
                                                                                          Mono<? extends T3> p3,
                                                                                          Mono<? extends T4> p4,
                                                                                          Mono<? extends T5> p5,
                                                                                          Mono<? extends T6> p6,
                                                                                          Mono<? extends T7> p7,
                                                                                          Mono<? extends T8> p8,
                                                                                          Mono<? extends T9> p9,
                                                                                          Mono<? extends T10> p10,
                                                                                          Mono<? extends T11> p11,
                                                                                          Mono<? extends T12> p12,
                                                                                          Mono<? extends T13> p13,
                                                                                          Functions.Function13<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, U> fun) {
        return Mono.zip(objects -> fun.apply_(objects), p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13);
    }

    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, U> Mono<U> zip(Mono<? extends T1> p1,
                                                                                               Mono<? extends T2> p2,
                                                                                               Mono<? extends T3> p3,
                                                                                               Mono<? extends T4> p4,
                                                                                               Mono<? extends T5> p5,
                                                                                               Mono<? extends T6> p6,
                                                                                               Mono<? extends T7> p7,
                                                                                               Mono<? extends T8> p8,
                                                                                               Mono<? extends T9> p9,
                                                                                               Mono<? extends T10> p10,
                                                                                               Mono<? extends T11> p11,
                                                                                               Mono<? extends T12> p12,
                                                                                               Mono<? extends T13> p13,
                                                                                               Mono<? extends T14> p14,
                                                                                               Functions.Function14<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, U> fun) {
        return Mono.zip(objects -> fun.apply_(objects), p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14);
    }

    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, U> Mono<U> zip(Mono<? extends T1> p1,
                                                                                                    Mono<? extends T2> p2,
                                                                                                    Mono<? extends T3> p3,
                                                                                                    Mono<? extends T4> p4,
                                                                                                    Mono<? extends T5> p5,
                                                                                                    Mono<? extends T6> p6,
                                                                                                    Mono<? extends T7> p7,
                                                                                                    Mono<? extends T8> p8,
                                                                                                    Mono<? extends T9> p9,
                                                                                                    Mono<? extends T10> p10,
                                                                                                    Mono<? extends T11> p11,
                                                                                                    Mono<? extends T12> p12,
                                                                                                    Mono<? extends T13> p13,
                                                                                                    Mono<? extends T14> p14,
                                                                                                    Mono<? extends T15> p15,
                                                                                                    Functions.Function15<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, U> fun) {
        return Mono.zip(objects -> fun.apply_(objects), p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15);
    }
}
