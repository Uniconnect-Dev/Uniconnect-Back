package com.uniConnect.member.security;

import com.uniConnect.member.repository.UsersRepository;
import com.uniConnect.member.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UsersRepository usersRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User u = usersRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return new CustomUser(u.getUserId(), u.getUsername(), u.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }
}
