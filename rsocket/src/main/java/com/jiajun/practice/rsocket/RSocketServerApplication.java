package com.jiajun.practice.rsocket;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * @author jiajun
 */
@SpringBootApplication
public class RSocketServerApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(RSocketServerApplication.class).web(WebApplicationType.NONE).run(args);
    }
}
