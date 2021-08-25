package com.jiajun.reactor.circuit;

import com.google.common.collect.Maps;
import lombok.Getter;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 每个id一个断路器
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
     * 所有时间窗口内的数据, key是s
     */
    private Map<Long, WindowBucket> buckets = Maps.newHashMapWithExpectedSize(bucketCount);

    /**
     * 所有buckets中请求数
     */
    private volatile long totalRequestCnt = 0L;

    /**
     * 所有buckets中错误数
     */
    private volatile long totalErrorCnt = 0L;

    /**
     * 所有buckets中错误率 [0-100]
     */
    private volatile int errorRatePct = 0;

    /**
     * 是否允许请求
     * 未熔断: isOpen=false
     *
     * @return
     */
    public boolean allowRequest() {
        if (isCircuitOpening()) {
            // 随机熔断掉部分请求
            return ThreadLocalRandom.current().nextInt(CIRCUIT_THRESHOLD_MAX) < circuitThreshold;
        } else {
            if (totalRequestCnt < 10) {
                // 总请求最小值: 样本数太少, 无法准确预估
                return true;
            }
            if (errorRatePct < 50) {
                // 超过一定的异常比例
                return true;
            }
            if (!isCircuitOpening()) {
                openCircuit(); // 触发熔断
            }
            return false;
        }
    }

    /**
     * 单线程每秒触发一次, 更新这段时间内的指标
     *
     * @param bucket
     */
    public void updateRollingWindowInfo(WindowBucket bucket) {
        if (bucket == null || !bucket.getId().equals(id)) {
            return;
        }
        // 窗口的buckets
        long currentIdx = System.currentTimeMillis() / 1000;
        buckets.entrySet().removeIf(secondResultEntry -> secondResultEntry.getKey() <= currentIdx - 10L); // 移除旧数据
        buckets.put(currentIdx, bucket);

        // 总请求计数
        totalRequestCnt = buckets.values().stream().map(WindowBucket::getRequestCnt).count();
        totalErrorCnt = buckets.values().stream().map(WindowBucket::getErrorCnt).count();
        if (totalRequestCnt == 0) {
            this.errorRatePct = 0;
        } else {
            this.errorRatePct = (int) (totalErrorCnt * 100 / totalRequestCnt);
        }

        // 熔断恢复策略:
        if (isCircuitOpening()) {
            final int defaultCircuitBreakerRecoverRatio = 50; // 每秒恢复数, 每秒恢复5%, 连续20s正常则完全恢复
            if (bucket.getErrorCnt() == 0) {
                // 最近一个窗口没有错误, 恢复
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
