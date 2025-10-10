package com.uniConnect.config;

import com.uniConnect.member.security.local.JwtAuthFilter;
import com.uniConnect.member.security.oauth.*;
import com.uniConnect.member.security.oauth.jwt.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.util.matcher.*;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import java.util.Set;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity(debug = true)
public class SecurityConfig {
    private final JwtAuthFilter jwtAuthFilter;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler successHandler;   // JWT/쿠키 세팅 및 redirect
    private final OAuth2FailureHandler failureHandler;
    private final AuthenticationConfiguration authenticationConfiguration;
    private final ClientRegistrationRepository clientRegistrationRepository;

    @Bean
    @Order(1)
    SecurityFilterChain oauthChain(HttpSecurity http) throws Exception {
        var def = new DefaultOAuth2AuthorizationRequestResolver(clientRegistrationRepository,
                "/oauth2/authorization");

        OAuth2AuthorizationRequestResolver resolver = new OAuth2AuthorizationRequestResolver() {
            @Override
            public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
                OAuth2AuthorizationRequest r = def.resolve(request);
                return customize(r);
            }
            @Override
            public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
                OAuth2AuthorizationRequest r = def.resolve(request, clientRegistrationId);
                return customize(r);
            }
            private OAuth2AuthorizationRequest customize(OAuth2AuthorizationRequest r) {
                if (r == null) return null;
                var extra = new java.util.HashMap<String, Object>(r.getAdditionalParameters());
                extra.put("prompt", "select_account");
                return OAuth2AuthorizationRequest.from(r)
                        .additionalParameters(extra)
                        .build();
            }
        };

        http
                .csrf(csrf -> csrf.disable())
                .securityMatcher("/login", "/logout", "/oauth2/**", "/", "/error")
                .authorizeHttpRequests(auth -> auth.requestMatchers(
                                        "/", "/error", "/oauth2/**", "/login"
                                ).permitAll()
                                .anyRequest().authenticated()
                )
                // 비로그인 접근 시 구글 로그인 URL로 redirect
                .exceptionHandling(e -> e.authenticationEntryPoint(
                        new org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint("/oauth2/authorization/google")
                ))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)) //stateless시 session c 막힘
                .oauth2Login(oauth -> oauth
                        //oauth 화면 노출
                        .loginPage("/oauth2/authorization/google")
                        .authorizationEndpoint(a -> a.authorizationRequestResolver(resolver))
                        .userInfoEndpoint(u -> u.userService(customOAuth2UserService))
                        .successHandler(successHandler)
                        .failureHandler(failureHandler)
                )
                .logout(logout -> logout
                                // 👇 logoutUrl()만 지정하면 내부적으로 자동 RequestMatcher 등록됨
                                .logoutUrl("/logout")
                                // 만약 프론트가 GET으로 호출한다면:
                                // .logoutRequestMatcher(request -> "GET".equals(request.getMethod()) && "/logout".equals(request.getRequestURI()))

                                .clearAuthentication(true)
                                .invalidateHttpSession(true)
                                //Bearer token은 server가 못지움
                                .deleteCookies("JSESSIONID", "ACCESS_TOKEN", "REFRESH_TOKEN")
                                .addLogoutHandler((req,res,auth) -> {
                                    // TODO: 서버 저장 Refresh 토큰/세션 삭제 + Access 토큰 블랙리스트 등록
                                    String authz = req.getHeader("Authorization");
                                    if (authz != null && authz.startsWith("Bearer ")) {
                                        String accessToken = authz.substring(7);
                                        // refreshTokenService.invalidateByAccess(accessToken);
                                        // tokenBlacklist.add(accessToken, jwtExpiry(accessToken));
                                    }
                                    // 세션 기반 AuthorizedClient 쓰는 경우 제거 예시:
                                })
                                //oidc 공급자 logout
                                .logoutSuccessHandler((req, res, auth) -> {
                                    // ★ 서버는 단지 성공만 알려준다. 프런트가 여기서 토큰을 지워야 함.
                                    res.setStatus(200);
                                    res.setContentType("application/json;charset=UTF-8");
                                    res.getWriter().write("{\"message\":\"logged out\"}");
                                })
                        //.logoutSuccessHandler(oidcLogoutSuccessHandler())
                )
                //basic 인증, formLogin x
                .httpBasic(b -> b.disable())
                .formLogin(f -> f.disable());
        return http.build();
    }

    @Bean
    @Order(2)
    SecurityFilterChain api(HttpSecurity http) throws Exception {
        RequestMatcher apiMatcher = apiRequestMatcher(http);
        http
                .securityMatcher(apiMatcher)
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {}) // 필요 시 CORS
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth ->
                auth.requestMatchers(
                        // Swagger & OpenAPI (springdoc)
                        "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/v3/api-docs.yaml",
                        "/swagger-resources/**", "/webjars/**", "/actuator/health", "/actuator/info",
                        "/auth/**"
                ).permitAll()
                //cors preflight
                .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, org.springframework.security.web.authentication.logout.LogoutFilter.class)
                .httpBasic(b -> b.disable())
                .formLogin(f -> f.disable())
                .exceptionHandling(e -> e
                        // API는 401/403을 JSON으로 주는 엔트리포인트/핸들러를 사용
                        .authenticationEntryPoint(new org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint())
                        .accessDeniedHandler(new org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler())
                );
        return http.build();
    }

    @Bean
    @Order(3)
    SecurityFilterChain catchAll(HttpSecurity http) throws Exception {

        // 1) API/XHR 요청 판별 매처 (JSON accept, Ajax 헤더 등)
        var jsonAccept = new org.springframework.security.web.util.matcher.RequestHeaderRequestMatcher(
                "Accept", "application/json");
        var ajax = new org.springframework.security.web.util.matcher.RequestHeaderRequestMatcher(
                "X-Requested-With", "XMLHttpRequest");
        var apiLike = new org.springframework.security.web.util.matcher.OrRequestMatcher(jsonAccept, ajax);

        // 2) 엔트리포인트 분기: API → 401(Bearer), 그 외 → OAuth 로그인으로 302
        var mappings = new java.util.LinkedHashMap<org.springframework.security.web.util.matcher.RequestMatcher,
                org.springframework.security.web.AuthenticationEntryPoint>();
        mappings.put(apiLike, new org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint());

        var delegatingEntryPoint =
                new org.springframework.security.web.authentication.DelegatingAuthenticationEntryPoint(mappings);
        delegatingEntryPoint.setDefaultEntryPoint(
                new org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint("/oauth2/authorization/google")
        );

        http
                // 캐치올
                .securityMatcher("/**")
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))

                // 화이트리스트 전역 허용(정적/스웨거 등). 나머지는 모두 인증 필요
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
//                                "/", "/error", "/login", "/logout", "/oauth2/**",
                                "/swagger-ui.html", "/swagger-ui/**",
                                "/v3/api-docs/**", "/v3/api-docs.yaml",
                                "/swagger-resources/**", "/webjars/**",
                                "/actuator/health", "/actuator/info",
                                "/css/**", "/js/**", "/images/**", "/assets/**", "/static/**", "/favicon.ico"
                        ).permitAll()
                        .anyRequest().authenticated()
                )

                // JWT 먼저 시도 (Bearer가 있으면 여기서 인증 완료)
                .addFilterBefore(jwtAuthFilter, org.springframework.security.web.authentication.logout.LogoutFilter.class)

                // 브라우저 케이스에선 fe baseUrl/login(OAuth2 로그인 플로우)로 보낼 수 있게 활성화
                .oauth2Login(oauth -> {
                    // 필요하면 resolver로 prompt=select_account 유지
                })

                // 인증 안 된 접근 시 "요청 성격"에 따라 분기
                .exceptionHandling(e -> e.authenticationEntryPoint(delegatingEntryPoint))

                .httpBasic(b -> b.disable())
                .formLogin(f -> f.disable());

        return http.build();
    }


    private RequestMatcher apiRequestMatcher(HttpSecurity http) {
        // Accept/Content-Type 로 JSON 요청 판별
        var cns = http.getSharedObject(org.springframework.web.accept.ContentNegotiationStrategy.class);
        var jsonByAccept = new org.springframework.security.web.util.matcher.MediaTypeRequestMatcher(
                cns, org.springframework.http.MediaType.APPLICATION_JSON);
        jsonByAccept.setUseEquals(true);      // JSON 만 정확히 매칭
        jsonByAccept.setIgnoredMediaTypes(Set.of(org.springframework.http.MediaType.ALL));

        // Authorization: Bearer ...
        RequestMatcher bearerAuth = request -> {
            String auth = request.getHeader("Authorization");
            return auth != null && auth.startsWith("Bearer ");
        };

        // AJAX 또는 커스텀 헤더
        RequestMatcher ajax = new org.springframework.security.web.util.matcher.RequestHeaderRequestMatcher(
                "X-Requested-With", "XMLHttpRequest");
        RequestMatcher customApiHeader = new org.springframework.security.web.util.matcher.RequestHeaderRequestMatcher(
                "X-API-Request", "true");

        // Content-Type: application/json (POST/PUT/PATCH 등)
        RequestMatcher jsonByContentType = request -> {
            String ct = request.getContentType();
            return ct != null && ct.startsWith(org.springframework.http.MediaType.APPLICATION_JSON_VALUE);
        };

        return new OrRequestMatcher(
                jsonByAccept, jsonByContentType, bearerAuth, ajax, customApiHeader
        );
    }

    @Bean
    public LogoutSuccessHandler oidcLogoutSuccessHandler() {
        var handler = new org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler(
                clientRegistrationRepository
        );
        handler.setPostLogoutRedirectUri("{baseUrl}/"); // 로그아웃 후 돌아올 URL
        return handler;
    }

    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
