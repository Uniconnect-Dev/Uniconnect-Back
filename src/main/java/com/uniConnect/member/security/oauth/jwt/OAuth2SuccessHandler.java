package com.uniConnect.member.security.oauth.jwt;

import com.uniConnect.member.entity.User;
import com.uniConnect.member.repository.UserRepository;
import com.uniConnect.member.security.oauth.OAuthAccountService;
import com.uniConnect.member.security.oauth.OAuthUserInfoFactory;
import com.uniConnect.member.security.oauth.dto.OAuthUserInfo;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final OAuthUserInfoFactory userInfoFactory;
    private final OAuthAccountService linkService;
    private final JwtTokenService jwtTokenService;

    @Value("${oauth2.success-redirect:/}")
    private String successRedirect;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest req, HttpServletResponse res,
                                        Authentication authentication) throws IOException {
        if (!(authentication instanceof OAuth2AuthenticationToken token)) {
            res.sendRedirect(successRedirect);
            return;
        }

        String registrationId = token.getAuthorizedClientRegistrationId(); // "google"
        Map<String, Object> attributes = token.getPrincipal().getAttributes();

        // 1) 프로바이더-중립 유저 정보
        OAuthUserInfo info = userInfoFactory.from(registrationId, attributes);

        // 2) DB 연동 (없으면 생성, 있으면 연결 반환)
        User user = linkService.linkOrCreateUser(info);

        // 3) JWT 발급 + 쿠키
        String access = jwtTokenService.createAccessToken(user);
        Cookie c = new Cookie("ACCESS_TOKEN", access);
        c.setHttpOnly(true);
        c.setSecure(false); // HTTPS면 true
        c.setPath("/");
        res.addCookie(c);

        // 4) 프론트로 리다이렉트
        res.sendRedirect(successRedirect);
    }
}
