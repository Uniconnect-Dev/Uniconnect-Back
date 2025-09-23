package com.uniConnect.users;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Entity
@Table(name = "users")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Users implements UserDetails {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long usersId;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    private String name;

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
