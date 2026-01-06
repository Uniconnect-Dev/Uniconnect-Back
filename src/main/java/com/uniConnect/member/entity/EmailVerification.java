package com.uniConnect.member.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "email_verifications")
public class EmailVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "verification_id")
    private Long verificationId;

    // 2) information
    @Column(name = "email", length = 120)
    private String email;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    //추가
    @Column(name = "code_hash", length = 255)
    private String codeHash;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;  // 코드 만료시간

    @Column(name = "verified")
    private Boolean verified = false;  // 인증 완료 여부

    @Column(name = "attempt_count")
    private Integer attemptCount = 0;  // 시도 횟수 (bruteforce 방지)

    @Column(name = "last_sent_at")
    private LocalDateTime lastSentAt;  // 마지막 코드 발송 시간 (재전송 제한용)

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    // 3) relations
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_id") // FK: users.user_id
//    private User user;
}
