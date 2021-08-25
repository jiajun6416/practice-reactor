package com.jiajun.reactor.circuit;

import lombok.Data;

/**
 * 滑动窗口中每秒一个桶
 *
 * @author jiajun
 */
@Data
public class WindowBucket {

    private String id; // 唯一标识

    private int requestCnt; // 总请求总数

    private int errorCnt; // 异常数

    public WindowBucket(String id) {
        this.id = id;
    }
}
