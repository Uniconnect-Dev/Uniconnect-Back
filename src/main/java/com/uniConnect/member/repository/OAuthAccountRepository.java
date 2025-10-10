package com.uniConnect.member.repository;

import com.uniConnect.member.entity.OAuthAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OAuthAccountRepository extends JpaRepository<OAuthAccount, Long> {
    Optional<OAuthAccount> findByProviderAndProviderUserId(String provider, String providerUserId);
    Optional<OAuthAccount> findByProviderAndEmail(String provider, String email);
    boolean existsByUserUserIdAndProvider(Long userId, String provider);
}
