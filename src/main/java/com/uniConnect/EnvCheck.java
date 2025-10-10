package com.uniConnect;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class EnvCheck implements CommandLineRunner {
    @Value("${AWS_ACCESS_KEY_ID:NOT_FOUND}")
    private String accessKey;

    @Override
    public void run(String... args) {
        System.out.println("🚀 Loaded AWS_ACCESS_KEY_ID = " + accessKey);
    }
}
