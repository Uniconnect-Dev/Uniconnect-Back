package com.uniConnect.users.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.*;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    // 화이트리스트(필터 건너뛰기)
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    private static final String[] WHITELIST = {
            "/auth/**", "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**"};

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        // OPTIONS(CORS freeflight), Whitelist path
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;
        for (String p : WHITELIST) {
            if (PATH_MATCHER.match(p, path)) return true;}
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        String authHeader = req.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // 토큰 없으면 그냥 통과 (익명 사용자)
            chain.doFilter(req, res); //통과: 다음 filter로
            return;
        }

        String token = authHeader.substring(7);
        try {
            String username = jwtUtil.extractUsername(token);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtUtil.isValid(token, userDetails.getUsername())) {
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    SecurityContext context = SecurityContextHolder.createEmptyContext();
                    context.setAuthentication(auth);
                    SecurityContextHolder.setContext(context);
                }
                // 토큰이 유효하지 않으면 "그냥 인증 없이" 진행 (중요 포인트!)
            }
            chain.doFilter(req, res);
        } catch (Exception e) {
            // ❌ 여기서 401을 쓰지 말자. 공개/permitAll 엔드포인트까지 막히는 원인.
            // log.debug("JWT parse/validate failed", e);
            chain.doFilter(req, res);
        }
    }
}