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

        String uri = request.getRequestURI();
        return uri.startsWith("/oauth2/")
                || uri.startsWith("/login/**")
                || uri.startsWith("/auth/")           // 성공/실패 페이지
                || uri.startsWith("/v3/api-docs")
                || uri.startsWith("/swagger-ui")
                || uri.startsWith("/swagger-resources")
                || uri.startsWith("/webjars")
                || uri.equals("/")
                || uri.startsWith("/css/")
                || uri.startsWith("/js/")
                || uri.startsWith("/images/")
                || uri.startsWith("/assets/")
                || uri.equals("/favicon.ico");
    }
    //read cookie
    private String resolveToken(HttpServletRequest req) {
        // 1) 헤더 우선
        String h = req.getHeader("Authorization");
        if (h != null && h.startsWith("Bearer ")) {
            return h.substring(7);
        }
        // 2) 쿠키에서도 시도
        if (req.getCookies() != null) {
            for (Cookie c : req.getCookies()) {
                if ("ACCESS_TOKEN".equals(c.getName()) && c.getValue() != null && !c.getValue().isBlank()) {
                    return c.getValue();
                }
            }
        }
        return null;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        try {
            String token = resolveToken(req); // 헤더 > 쿠키(ACCESS_TOKEN)
            if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // 1) 서명/만료 검증 (예외 나면 캐치됨)
                if (jwtUtil.isValid(token)) { // 서명/만료만 체크하는 메서드
                    // 2) principal: username(이메일) 우선, 없으면 subject
                    String principal = jwtUtil.extractClaim(token, "username");
                    if (principal == null || principal.isBlank()) {
                        principal = jwtUtil.extractSubject(token); // sub
                    }

                    // 3) 권한도 토큰에서 읽기 (예: "role" 클레임)
                    String role = jwtUtil.extractClaim(token, "role"); // 예: "StudentOrg"
                    var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

                    // 4) DB 조회 없이 인증객체 생성
                    var auth = new UsernamePasswordAuthenticationToken(principal, null, authorities);
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));

                    var context = SecurityContextHolder.createEmptyContext();
                    context.setAuthentication(auth);
                    SecurityContextHolder.setContext(context);
                }
            }
        } catch (Exception ignored) { /* 절대 여기서 401 쓰지 말 것 */ }

        chain.doFilter(req, res);
    }
}