package com.uniConnect.member.entity;

import com.uniConnect.member.enums.UserRole;
import com.uniConnect.member.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "users",
        uniqueConstraints = @UniqueConstraint(name = "uk_users_username", columnNames = "username"))
public class User implements UserDetails {

    // 1) default
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    // 2) information
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

    // 3) relations - (양방향 컬렉션 필요 시 추가)

    // ===== UserDetails 구현 메서드 =====
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 권한을 간단히 "ROLE_USER"로 고정
        return Collections.singleton(() -> "ROLE_USER");
    }

    @Override
    public boolean isAccountNonExpired() {return true;} // 계정 만료 안 됨

    @Override
    public boolean isAccountNonLocked() {return true;} // 계정 잠김 아님

    @Override
    public boolean isCredentialsNonExpired() {return true;} // 비밀번호 만료 안 됨

    @Override
    public boolean isEnabled() {return true;} // 계정 활성화됨
}
