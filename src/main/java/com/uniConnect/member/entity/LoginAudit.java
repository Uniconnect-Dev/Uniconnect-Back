package com.uniConnect.member.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "login_audits")
public class LoginAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "audit_id")
    private Long auditId;

    // info
    @Column(name = "ip", length = 45)
    private String ip;

    @Column(name = "device_info", length = 100)
    private String deviceInfo;

    @Column(name = "success")
    private Boolean success;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // relation
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
