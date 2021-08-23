package com.jiajun.practice.rsocket.vo;

import lombok.Data;

import java.util.List;

/**
 * @author jiajun
 */
@Data
public class MsgVo {

    private Long id;

    private String name;

    private List<Long> users;
}
