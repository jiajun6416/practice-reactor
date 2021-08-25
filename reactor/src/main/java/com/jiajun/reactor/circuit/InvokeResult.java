package com.jiajun.reactor.circuit;

import lombok.Data;

/**
 * @author jiajun
 */
@Data
public class InvokeResult {

    private String id;

    private boolean success; // 成功或失败

    private int costTime; // 耗时
}
