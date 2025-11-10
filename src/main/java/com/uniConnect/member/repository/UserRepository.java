package com.uniConnect.member.repository;

import com.uniConnect.member.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByLocalCredential_LoginId(String loginId);
}
