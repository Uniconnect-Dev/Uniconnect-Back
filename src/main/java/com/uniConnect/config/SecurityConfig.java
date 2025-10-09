package com.uniConnect.config;

import com.uniConnect.member.security.local.AuthService;
import com.uniConnect.member.security.local.JwtAuthFilter;
import com.uniConnect.member.security.oauth.*;
import com.uniConnect.member.security.oauth.jwt.*;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static com.uniConnect.member.security.local.AuthService.deleteCookie;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity(debug = true)
public class SecurityConfig {
    private final JwtAuthFilter jwtAuthFilter;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler successHandler;   // JWT/쿠키 세팅 및 redirect
    private final OAuth2FailureHandler failureHandler;

    @Bean
    @Order(1)
    SecurityFilterChain oauthChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/oauth2/**", "/login/oauth2/**", "/auth/**")
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(u -> u.userService(customOAuth2UserService))
                        .successHandler(successHandler)
                        .failureHandler(failureHandler)
                )
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)) //stateless시 session c 막힘
                .httpBasic(b -> b.disable())
                .formLogin(f -> f.disable());
        return http.build();
    }

    @Bean
    @Order(2)
    SecurityFilterChain security(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                .ignoringRequestMatchers("/auth/**") // 로그인/회원가입은 CSRF 검사 제외
                )
                .cors(cors -> {}) // CorsConfig의 bean 사용
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/", "/error",
                                // Swagger & OpenAPI (springdoc)
                                "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/v3/api-docs.yaml",
                                "/swagger-resources/**", "/webjars/**",
                                // Auth & public endpoints
                                "/auth/**", "/actuator/health", "/actuator/info",
                                // "login", "/login/all", "/signup", "/nginx-check", "/s3/**",
                                // Static assets
                                "/css/**", "/js/**", "/images/**", "/assets/**", "/static/**", "/favicon.ico"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .logout(l -> l
                        .logoutUrl("/auth/logout")
                        .logoutSuccessHandler((req, res, auth) -> {
                            // ACCESS_TOKEN / REFRESH_TOKEN 모두 지우기
                            deleteCookie(res, "ACCESS_TOKEN");
                            deleteCookie(res, "REFRESH_TOKEN");
                            res.setStatus(HttpServletResponse.SC_NO_CONTENT); // 204
                        })
                        .deleteCookies("ACCESS_TOKEN", "REFRESH_TOKEN") // 보조
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                // basic auth off (optional)
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(form -> form.disable());

        return http.build();
    }

    private final AuthenticationConfiguration authenticationConfiguration;

    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
