package com.uniConnect.member.security.oauth;

import com.uniConnect.member.security.oauth.dto.GoogleUserInfo;
import com.uniConnect.member.security.oauth.dto.OAuthUserInfo;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class OAuthUserInfoFactory {
    public OAuthUserInfo from(String registrationId, Map<String,Object> attributes) {
        return switch (registrationId) {
            case "google" -> new GoogleUserInfo(attributes);
//            case "kakao"  -> new KakaoUserInfo(attributes);
            default -> throw new OAuth2AuthenticationException("Unsupported provider: " + registrationId);
        };
    }
}
