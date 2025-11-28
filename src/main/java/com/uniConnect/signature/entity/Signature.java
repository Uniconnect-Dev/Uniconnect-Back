package com.uniConnect.signature.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Signature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long signatureId;

    private Long userId;              // 서명자
    private String signatureHash;     // 검증된 서명 해시
    private Long timestamp;           // 서명 시간

    private Instant createdAt = Instant.now();
}