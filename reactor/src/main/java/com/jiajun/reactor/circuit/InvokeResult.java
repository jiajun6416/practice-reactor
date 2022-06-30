package com.jiajun.reactor.circuit;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 滑动窗口每个桶中的事件
 * @author jiajun
 */
@Data
@AllArgsConstructor
public class InvokeResult {

    private String id;

    private boolean success; // 成功或失败

    private int costTime; // 耗时

}
