package com.jiajun.practice.reactor.webflux;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author jiajun
 */
@RestController
@RequestMapping("webmvc")
public class HelloWebmvcController {

    @GetMapping("query1/{ts}")
    public Object query1(String ts) {
        return "helloWebMvc: " + ts;
    }
}
