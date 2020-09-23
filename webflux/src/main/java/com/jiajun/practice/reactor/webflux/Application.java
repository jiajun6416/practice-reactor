package com.jiajun.practice.reactor.webflux;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.reactive.config.WebFluxConfigurer;

/**
 * @author jiajun
 */
@SpringBootApplication
public class Application implements WebFluxConfigurer {

    public static void main(String[] args) {
        new SpringApplication(Application.class).run(args);
    }
}
