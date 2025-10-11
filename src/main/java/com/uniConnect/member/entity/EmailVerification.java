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

    @Column(name = "code", length = 20)
    private String code;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    // 3) relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // FK: users.user_id
    private User user;
}
