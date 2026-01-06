package com.uniConnect.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
//외부 http 호출용
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder //server 연결, 응답data 시도: timeout 설정을 client에서
//                .setConnectTimeout(Duration.ofSeconds(5))
//                .setReadTimeout(Duration.ofSeconds(10))
                .build();
    }
}

