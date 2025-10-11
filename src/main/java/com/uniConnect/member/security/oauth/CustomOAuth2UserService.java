package com.uniConnect.member.security.oauth;

import com.uniConnect.member.entity.User;
import com.uniConnect.member.security.oauth.dto.OAuthUserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Slf4j // ⭐ 로깅 추가
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final OAuthUserInfoFactory userInfoFactory;
    private final OAuthAccountService linkService;

    //Google로부터 user_정보 받아와 app_user로 바꿈
    @Override
    public OAuth2User loadUser(OAuth2UserRequest req) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(req);
        String regId = req.getClientRegistration().getRegistrationId();

        log.info("🔵 OAuth2 login started - provider: {}", regId);
        log.debug("OAuth2 attributes: {}", oAuth2User.getAttributes());

        OAuthUserInfo info = userInfoFactory.from(regId, oAuth2User.getAttributes());
        log.info("🔵 Extracted user info - provider: {}, providerId: {}, email: {}", 
                 info.provider(), info.providerUserId(), info.email());

        User user = linkService.linkOrCreateUser(info);
        log.info("✅ User linked/created - userId: {}, username: {}, role: {}", 
                 user.getUserId(), user.getUsername(), user.getRole());

        // Security Principal 생성 (권한 부여)
        Collection<GrantedAuthority> auths = List.of((GrantedAuthority) () -> "ROLE_" + user.getRole().name());

        // nameAttributeKey는 provider별 기본 키 사용
        String nameKey = req.getClientRegistration().getProviderDetails()
                .getUserInfoEndpoint().getUserNameAttributeName();

        return new DefaultOAuth2User(auths, oAuth2User.getAttributes(), nameKey);
    }
}
