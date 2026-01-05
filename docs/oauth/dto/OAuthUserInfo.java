package com.uniConnect.member.security.oauth.dto;

import java.util.Map;

public interface OAuthUserInfo {
    String provider();         // "google", "kakao", "naver" ...
    String providerUserId();   // sub, kakao id 등
    String email();            // 없으면 null 허용
    String username();
    String picture();
    Map<String, Object> attributes();
}
