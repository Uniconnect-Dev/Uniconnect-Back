package com.uniConnect;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class UniConnectApplication {

	public static void main(String[] args) {
        //env 파일 없어도 되도록 수정
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing().load();
        dotenv.entries().forEach(entry -> {
            System.setProperty(entry.getKey(), entry.getValue());
        });
		SpringApplication.run(UniConnectApplication.class, args);
	}

}
