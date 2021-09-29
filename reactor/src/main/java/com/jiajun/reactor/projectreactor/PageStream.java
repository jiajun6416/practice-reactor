package com.jiajun.reactor.projectreactor;

import reactor.core.publisher.Flux;

import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
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
        Spliterator<List<T>> spliterator = Spliterators.spliteratorUnknownSize(new Iterator<List<T>>() {
            int offset;

            List<T> result;

            boolean lastPage;

            @Override
            public boolean hasNext() {
                if (lastPage) {
                    return false;
                }
                result = offsetLimitQuery.apply(offset, pageSize);
                boolean haxNext = result != null && !result.isEmpty();
                lastPage = haxNext && result.size() < pageSize;
                return haxNext;
            }

            @Override
            public List<T> next() {
                if (result == null || result.isEmpty()) {
                    throw new UnsupportedOperationException();
                }
                offset += result.size();
                return result;
            }
        }, Spliterator.NONNULL);
        return StreamSupport.stream(spliterator, false);
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
        Spliterator<List<T>> spliterator = Spliterators.spliteratorUnknownSize(new Iterator<List<T>>() {
            List<T> result;

            T lastItem;

            boolean lastPage;

            @Override
            public boolean hasNext() {
                if (lastPage) {
                    return false;
                }
                result = lastItemLimitQuery.apply(lastItem, pageSize);

                boolean hasNext = result != null && !result.isEmpty();
                lastPage = hasNext && result.size() < pageSize;
                return hasNext;
            }

            @Override
            public List<T> next() {
                if (result == null || result.isEmpty()) {
                    throw new UnsupportedOperationException();
                }
                lastItem = result.get(result.size() - 1);
                return result;
            }
        }, Spliterator.NONNULL);
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
