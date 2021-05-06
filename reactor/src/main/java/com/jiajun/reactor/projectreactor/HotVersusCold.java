package com.jiajun.reactor.projectreactor;

/**
 * Cold: 没有订阅者则永远不会生成序列
 * Hot: 没有订阅者仍然会生成序列, 新加入的订阅者可以选择从头消费or只消费最新的
 *
 * @author jiajun
 * https://projectreactor.io/docs/core/release/reference/index.html#reactor.hotCold
 */
public class HotVersusCold {
}
