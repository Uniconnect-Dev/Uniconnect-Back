package com.uniConnect.member.entity;

import com.uniConnect.member.enums.UserRole;
import com.uniConnect.member.enums.UserStatus;
import com.uniConnect.studentOrg.entity.StudentOrg;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "users",
        uniqueConstraints = @UniqueConstraint(name = "uk_users_username", columnNames = "username")
)
public class User implements UserDetails {

    // 1) 기본 식별자
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    // 2) 로그인 및 권한 정보
    @Column(name = "username", length = 50, nullable = false)
    private String username;

    @Column(name = "password_hash", length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 20)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private UserStatus status;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // 3) 연관관계 설정

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OAuthAccount> oauthAccounts = new ArrayList<>();

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private LocalCredential localCredential;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_org_id")
    private StudentOrg studentOrg;

    // ===== UserDetails 구현부 =====

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // DB에 저장된 UserRole 값을 기반으로 권한 생성
        if (this.role != null) {
            return List.of((GrantedAuthority) () -> "ROLE_" + this.role.name());
        }
        return List.of((GrantedAuthority) () -> "ROLE_USER");
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        // 만약 UserStatus를 기준으로 계정 활성화 제어하고 싶으면 이렇게 변경 가능
        return this.status == null || this.status == UserStatus.Active;
    }

    // ===== 헬퍼 메서드 =====

    public void addOAuthAccount(OAuthAccount account) {
        this.oauthAccounts.add(account);
        account.setUser(this);
    }

    public void setLocalCredential(LocalCredential credential) {
        this.localCredential = credential;
        credential.setUser(this);
    }
}
