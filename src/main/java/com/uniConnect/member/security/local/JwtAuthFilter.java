package com.uniConnect.member.security.local;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.*;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    private static final String[] WHITELIST = {
            "/auth/**", 
            "/swagger-ui/**", 
            "/swagger-ui.html", 
            "/v3/api-docs/**",
            "/oauth2/**",           // ⭐ OAuth2 authorization 경로
            "/login/oauth2/**"      // ⭐ OAuth2 콜백 경로
    };

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        
        // OPTIONS (CORS preflight)
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        
        // Whitelist patterns 체크
        for (String pattern : WHITELIST) {
            if (PATH_MATCHER.match(pattern, uri)) {
                return true;
            }
        }

        // 추가 정적 리소스 경로
        return uri.equals("/")
                || uri.equals("/login")
                || uri.equals("/logout")
                || uri.startsWith("/error")
                || uri.startsWith("/css/")
                || uri.startsWith("/js/")
                || uri.startsWith("/images/")
                || uri.startsWith("/assets/")
                || uri.startsWith("/static/")
                || uri.equals("/favicon.ico");
    }

    private String resolveToken(HttpServletRequest req) {
        // 1) Authorization 헤더 우선
        String header = req.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        
        // 2) 쿠키에서 ACCESS_TOKEN 읽기
        if (req.getCookies() != null) {
            for (Cookie cookie : req.getCookies()) {
                if ("ACCESS_TOKEN".equals(cookie.getName()) 
                    && cookie.getValue() != null 
                    && !cookie.getValue().isBlank()) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        try {
            String token = resolveToken(req);
            
            if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                if (jwtUtil.isValid(token)) {
                    // ✅ JWT에서 필요한 정보 추출
                    String subject = jwtUtil.extractSubject(token);
                    String username = jwtUtil.extractClaim(token, "username");
                    String role = jwtUtil.extractClaim(token, "role");
                    String userIdStr = jwtUtil.extractClaim(token, "userId");

                    Long userId = userIdStr != null ? Long.parseLong(userIdStr) : null;
                    if (role == null || role.isBlank()) {
                        role = "USER";
                    }

                    var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

                    // ✅ CustomUser 객체 생성
                    CustomUser customUser = new CustomUser(
                            userId,
                            username != null ? username : subject,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + role))
                    );

                    // ✅ CustomUser를 Principal로 저장
                    var auth = new UsernamePasswordAuthenticationToken(
                            customUser,  // ✅ CustomUser 객체!
                            null,
                            customUser.getAuthorities()
                    );

                    // SecurityContext 설정
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        } catch (Exception e) {
            // JWT 검증 실패 시 로그만 남기고 계속 진행
            logger.debug("JWT validation failed: " + e.getMessage());
        }

        chain.doFilter(req, res);
    }
}