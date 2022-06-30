package com.jiajun.reactor.circuit;

import com.google.common.collect.Maps;
import lombok.Getter;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 使用id标识资源, 每个资源一个熔断器
 * 窗口大小: 10s
 * 熔断的阈值: 10s内请求数 >= 10 && 异常比例 >= 50
 * 熔断的缓慢恢复: 每10s内没有异常(没有流量)恢复50的流量
 *
 * @author jiajun
 */
@Getter
public class CircuitBreaker {

    private static final int bucketCount = 10;

    /**
     * 目标id
     */
    private String id;

    /**
     * 熔断阈值，用于计算熔断比例
     * <p>
     * >=1000 表示不需要熔断
     * <1000  表示需要熔断
     */
    private volatile int circuitThreshold = 1000;

    /**
     * 大于该值则表示熔断关闭
     * 小于表示熔断开启
     */
    private static final int CIRCUIT_THRESHOLD_MAX = 1000;

    /**
     * 所有时间窗口内的数据, key是s.
     */
    private Map<Long, WindowBucket> buckets = Maps.newHashMapWithExpectedSize(bucketCount);

    /**
     * 窗口内所有请求
     */
    private volatile long totalRequestCnt = 0L;

    /**
     * 窗口内失败数
     */
    private volatile long totalErrorCnt = 0L;

    /**
     * 窗口内失败率 [0-100]
     */
    private volatile int errorRatePct = 0;

    public CircuitBreaker(String id) {
        this.id = id;
    }

    /**
     * 是否允许请求, 即判断熔断器是否关闭
     *
     * @return
     */
    public boolean allowRequest() {
        boolean allowRequest = true;
        if (isCircuitOpening()) {
            // 随机熔断掉部分请求
            allowRequest = ThreadLocalRandom.current().nextInt(CIRCUIT_THRESHOLD_MAX) < circuitThreshold;
        } else {
            if (totalRequestCnt >= 10 && errorRatePct >= 50) {
                // 触发熔断的条件: 窗口内请求数 >= 阈值 && 失败率 >= 阈值
                if (!isCircuitOpening()) {
                    openCircuit();
                }
                allowRequest = false;
            }
        }
        if (!allowRequest) {
            // todo 记录
            System.out.printf("api [%s] circuit info. totalReq: %s, totalError: %s, errorRate: %s \n", id, totalRequestCnt, totalErrorCnt, errorRatePct);
        }
        return allowRequest;
    }

    /**
     * 单线程每秒触发一次, 更新这段时间内的指标
     *
     * @param bucket
     */
    public void updateNewBucket(WindowBucket bucket) {
        if (bucket == null || !bucket.getId().equals(id)) {
            return;
        }

        // 窗口的buckets
        long currentIdx = System.currentTimeMillis() / 1000;
        buckets.entrySet().removeIf(secondResultEntry -> secondResultEntry.getKey() <= currentIdx - 10L); // 移除旧数据, 按照时间移除
        buckets.put(currentIdx, bucket);

        // 总请求计数
        totalRequestCnt = buckets.values().stream().map(WindowBucket::getRequestCnt).mapToInt(Integer::intValue).sum();
        totalErrorCnt = buckets.values().stream().map(WindowBucket::getErrorCnt).mapToInt(Integer::intValue).sum();
        if (totalRequestCnt == 0) {
            this.errorRatePct = 0;
        } else {
            this.errorRatePct = (int) (totalErrorCnt * 100 / totalRequestCnt);
        }

        // 熔断恢复策略: 缓慢恢复策略
        if (isCircuitOpening()) {
            final int defaultCircuitBreakerRecoverRatio = 50; // 每秒恢复的比例
            if (bucket.getErrorCnt() == 0) {
                // 最近一个窗口内没有异常: 没请求或请求了没异常
                circuitThreshold += defaultCircuitBreakerRecoverRatio;
                if (circuitThreshold > CIRCUIT_THRESHOLD_MAX) {
                    circuitThreshold = CIRCUIT_THRESHOLD_MAX;
                }
            } else {
                // 仍然有异常
                circuitThreshold -= defaultCircuitBreakerRecoverRatio;
                if (circuitThreshold < 0) {
                    circuitThreshold = 0;
                }
            }
        }
    }

    private boolean isCircuitOpening() {
        return circuitThreshold < CIRCUIT_THRESHOLD_MAX;
    }

    private void openCircuit() {
        this.circuitThreshold = 0;
    }
}
