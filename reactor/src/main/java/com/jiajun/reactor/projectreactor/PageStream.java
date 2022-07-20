package com.jiajun.reactor.projectreactor;

import reactor.core.publisher.Flux;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * 扫表时使用
 *
 * @author jiajun
 */
public class PageStream {

    /**
     * 基于 [offset,n] 方式的分页, 在offset很大时候性能很差
     *
     * @param <T>
     */
    @FunctionalInterface
    public interface OffsetLimitQuery<T> extends BiFunction<Integer, Integer, List<T>> {

        @Override
        List<T> apply(Integer offset, Integer limit);
    }

    /**
     * 基于 [offset,n] 方式的分页
     * Iterator实现
     *
     * @param pageSize
     * @param offsetLimitQuery
     * @param <T>
     * @return
     */
    public static <T> Stream<List<T>> byPageLimit(final int pageSize, OffsetLimitQuery<T> offsetLimitQuery) {
        Iterator<List<T>> iterator = new Iterator<>() {
            int offset;

            List<T> next;

            boolean end;

            @Override
            public boolean hasNext() {
                if (next != null && !next.isEmpty()) {
                    return true;
                }
                if (end) {
                    return false;
                }
                List<T> list = offsetLimitQuery.apply(offset, pageSize);
                end = list == null || list.size() < pageSize;
                next = list;
                return next != null && !next.isEmpty();
            }

            @Override
            public List<T> next() {
                if (next == null || next.isEmpty()) {
                    throw new NoSuchElementException();
                }
                offset += next.size();
                List<T> result = next;
                next = null;
                return result;
            }
        };
        return createStreamFromIterator(iterator);
    }

    /**
     * 基于 [offset,n] 方式的分页
     * flux实现
     *
     * @param pageSize
     * @param offsetLimitQuery
     * @param <T>
     * @return
     */
    public static <T> Stream<List<T>> byPageLimitV2(final int pageSize, OffsetLimitQuery<T> offsetLimitQuery) {
        Flux<List<T>> flux = Flux.generate(() -> 0, (offset, sink) -> {
            List<T> result = offsetLimitQuery.apply(offset, pageSize);
            if (result == null || result.isEmpty()) {
                sink.complete();
                return null;
            }
            sink.next(result);
            if (result.size() < pageSize) {
                sink.complete();
                return null;
            }
            return offset + result.size();
        });
        return flux.toStream(1); // batchSize设置为1, 避免缓存太多item, 造成OOM
    }

    /**
     * 每次分页查询的时候带上上一次最后的结果, 性能更好.
     *
     * @param <T>
     */
    @FunctionalInterface
    public interface LastItemLimitQuery<T> extends BiFunction<T, Integer, List<T>> {

        @Override
        List<T> apply(T lastItem, Integer limit);
    }

    /**
     * 推荐, 性能更好.
     * 基于Iterator实现
     *
     * @param offsetLimitQuery
     * @param <T>
     * @return
     */
    public static <T> Stream<List<T>> byLastLimit(final int pageSize, LastItemLimitQuery<T> lastItemLimitQuery) {
        Iterator<List<T>> iterator = new Iterator<>() {

            List<T> next;

            T last;

            boolean end;

            @Override
            public boolean hasNext() {
                if (next != null && !next.isEmpty()) {
                    return true;
                }
                if (end) {
                    return false;
                }
                List<T> list = lastItemLimitQuery.apply(last, pageSize);
                end = list == null || list.size() < pageSize;
                next = list;
                return next != null && !next.isEmpty();
            }

            @Override
            public List<T> next() {
                if (next == null || next.isEmpty()) {
                    throw new NoSuchElementException();
                }
                last = next.get(next.size() - 1);
                List<T> result = next;
                next = null;
                return result;
            }
        };
        return createStreamFromIterator(iterator);
    }

    /**
     * 将迭代器转成Stream. {@link org.springframework.data.util.StreamUtils#createStreamFromIterator(java.util.Iterator<T>)}
     * @param iterator
     * @param <T>
     * @return
     */
    public static <T> Stream<T> createStreamFromIterator(Iterator<T> iterator) {
        Spliterator<T> spliterator = Spliterators.spliteratorUnknownSize(iterator, Spliterator.NONNULL);
        return StreamSupport.stream(spliterator, false);
    }

    /**
     * 推荐, 性能更好.
     * 基于Flux实现
     *
     * @param lastItemLimitQuery
     * @param <T>
     * @return
     */
    public static <T> Stream<List<T>> byLastLimitV2(final int pageSize, LastItemLimitQuery<T> lastItemLimitQuery) {
        Flux<List<T>> flux = Flux.generate(() -> null, (lastItem, sink) -> {
            List<T> result = lastItemLimitQuery.apply((T) lastItem, pageSize);
            if (result == null || result.isEmpty()) {
                sink.complete();
                return null;
            }
            sink.next(result);
            if (result.size() < pageSize) {
                sink.complete();
                return null;
            }
            return result.get(result.size() - 1);
        });
        return flux.toStream(1); // batchSize=1, 避免缓存太多元素
    }
}
