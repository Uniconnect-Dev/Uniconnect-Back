package com.uniConnect.member.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter @Setter
@NoArgsConstructor          // ★ 필수
@AllArgsConstructor
@Builder
@Entity
@Table(name = "oauth_accounts",
        uniqueConstraints = {
                @UniqueConstraint(columnNames={"provider","providerUserId"}),
                @UniqueConstraint(columnNames={"provider","email"})
        })
public class OAuthAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false) @JoinColumn(name="user_id")
    private User user;

    @Column(nullable=false) private String provider;        // "google","kakao","naver" ...
    @Column(nullable=false) private String providerUserId;  // sub, kakao id 등
    private String email;                                   // 공급자 이메일(없을 수 있음)
    private String pictureUrl;

    // 토큰 보관이 필요하면 암호화 저장(선택)
    private String refreshTokenEnc;
    private Instant linkedAt = Instant.now();
}
