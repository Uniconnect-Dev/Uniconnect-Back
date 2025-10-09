package com.uniConnect.member.security.oauth;

import com.uniConnect.member.entity.OAuthAccount;
import com.uniConnect.member.entity.User;
import com.uniConnect.member.enums.UserRole;
import com.uniConnect.member.enums.UserStatus;
import com.uniConnect.member.repository.OAuthAccountRepository;
import com.uniConnect.member.repository.UserRepository;
import com.uniConnect.member.security.oauth.dto.OAuthUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

// OAuthAccountService.java
@Service
@RequiredArgsConstructor
public class OAuthAccountService {

    private final OAuthAccountRepository oauthRepo;
    private final UserRepository userRepo;

    @Transactional
    public User linkOrCreateUser(OAuthUserInfo info) {
        // 1) provider+providerUserId 우선 매칭
        Optional<OAuthAccount> existing = oauthRepo.findByProviderAndProviderUserId(info.provider(), info.providerUserId());
        if (existing.isPresent()) return existing.get().getUser();

        // 2) email 매칭 (동일 이메일로 로컬 가입/다른 소셜 연결된 사용자에 추가 연결)
        if (info.email() != null) {
            Optional<User> byEmailAsUsername = userRepo.findByUsername(info.email());
            if (byEmailAsUsername.isPresent()) {
                return createLink(byEmailAsUsername.get(), info);
            }
            // provider+email로 이미 기록된 OAuthAccount가 있는지도 체크
            Optional<OAuthAccount> byProvEmail = oauthRepo.findByProviderAndEmail(info.provider(), info.email());
            if (byProvEmail.isPresent()) return byProvEmail.get().getUser();
        }

        // 3) 신규 User 생성 후 연결
        User user = userRepo.save(User.builder()
                .username(info.email() != null ? info.email() : info.provider() + "_" + info.providerUserId())
                .role(UserRole.StudentOrg)
                .status(UserStatus.Active)// 프로젝트 enum에 맞게
                .build());

        return createLink(user, info);
    }

    private User createLink(User user, OAuthUserInfo info) {
        OAuthAccount acc = OAuthAccount.builder()
                .user(user)
                .provider(info.provider())
                .providerUserId(info.providerUserId())
                .email(info.email())
                .pictureUrl(info.picture())
                .build();
        oauthRepo.save(acc);

        return user;
    }
}
