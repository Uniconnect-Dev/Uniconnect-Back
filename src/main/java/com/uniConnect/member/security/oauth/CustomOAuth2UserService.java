package com.uniConnect.member.security.oauth;

import com.uniConnect.member.entity.User;
import com.uniConnect.member.security.oauth.dto.OAuthUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final OAuthUserInfoFactory userInfoFactory;
    private final OAuthAccountService linkService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest req) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(req);
        String regId = req.getClientRegistration().getRegistrationId();

        OAuthUserInfo info = userInfoFactory.from(regId, oAuth2User.getAttributes());
        User user = linkService.linkOrCreateUser(info);

        // Security Principal 생성 (권한 부여)
        Collection<GrantedAuthority> auths = List.of((GrantedAuthority) () -> "ROLE_" + user.getRole().name());

        // nameAttributeKey는 provider별 기본 키 사용
        String nameKey = req.getClientRegistration().getProviderDetails()
                .getUserInfoEndpoint().getUserNameAttributeName();

        return new DefaultOAuth2User(auths, oAuth2User.getAttributes(), nameKey);
    }
}
