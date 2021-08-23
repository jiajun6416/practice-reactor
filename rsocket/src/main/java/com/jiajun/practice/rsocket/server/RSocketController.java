package com.jiajun.practice.rsocket.server;

import com.jiajun.practice.rsocket.vo.MsgVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

/**
 * @author jiajun
 */
@Slf4j
@Controller
public class RSocketController {

    @MessageMapping("fire-and-forget")
    public Mono<Void> fireAndForget(MsgVo msgVo) {
        log.info("fire-and-forget: {}", msgVo);
        return Mono.empty();
    }
}
