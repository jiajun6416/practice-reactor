package com.jiajun.reactor.circuit;

import lombok.Data;

/**
 * 滑动窗口中的桶
 *
 * @author jiajun
 */
@Data
public class WindowBucket {

    private String id; // 唯一标识

    public int requestCnt; // 总请求总数

    public int errorCnt; // 异常数

    public WindowBucket(String id) {
        this.id = id;
    }
}
