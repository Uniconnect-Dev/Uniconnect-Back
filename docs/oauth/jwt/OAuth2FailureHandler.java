package com.uniConnect.member.security.oauth.jwt;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class OAuth2FailureHandler implements AuthenticationFailureHandler {

    @Value("${oauth2.failure-redirect}")
    private String failureRedirect;

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest req,
            HttpServletResponse res,
            AuthenticationException ex
    ) throws IOException, ServletException { // ✅ ServletException 추가
        String redirect = failureRedirect != null ? failureRedirect : "/login";
        res.sendRedirect(redirect + "?error=" +
                URLEncoder.encode(ex.getMessage(), StandardCharsets.UTF_8));
    }
}