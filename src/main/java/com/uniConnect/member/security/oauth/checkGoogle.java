package com.uniConnect.member.security.oauth;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

public class checkGoogle {
    @Bean
    ApplicationRunner checkGoogle(ClientRegistrationRepository repo) {
        return args -> {
            var google = repo.findByRegistrationId("google");
            System.out.println("== OAUTH CLIENT: google registered? " + (google != null));
            if (google != null) {
                System.out.println("redirectUri: " + google.getRedirectUri());
                System.out.println("scopes: " + google.getScopes());
            }
        };
    }
}
