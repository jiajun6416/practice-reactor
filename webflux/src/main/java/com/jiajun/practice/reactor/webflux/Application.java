package com.jiajun.practice.reactor.webflux;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.http.codec.CodecsAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.WebFluxAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.config.PathMatchConfigurer;
import org.springframework.web.reactive.config.WebFluxConfigurer;

import java.text.SimpleDateFormat;

/**
 * @author jiajun
 */
@SpringBootApplication
public class Application implements WebFluxConfigurer {

    public static void main(String[] args) {
        new SpringApplication(Application.class).run(args);
    }

    /**
     * @see CodecsAutoConfiguration
     * 支持自定义messageCodec.
     * @see WebFluxAutoConfiguration 时会引入CodecCustomizer, 作用在 ServerCodecConfigurer 上
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        return objectMapper;
    }

    @Override
    public void configurePathMatching(PathMatchConfigurer configurer) {
        configurer.addPathPrefix("talent-web", it -> true); // 所有的接口添加基础路径
    }

}

