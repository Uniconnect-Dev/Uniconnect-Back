package com.uniConnect.member.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "local_credentials",
        uniqueConstraints = @UniqueConstraint(columnNames = {"loginId"}))
public class LocalCredential {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //무조건 user있어야
    @OneToOne(optional=false) @JoinColumn(name="user_id")
    private User user;

    @Column(nullable=false, unique=true)
    private String loginId;        // 화면에서 쓰는 "아이디" (이메일과 별개)

    @Column(nullable=false)
    private String passwordHash;   // BCrypt 등
}