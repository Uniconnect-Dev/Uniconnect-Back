package com.uniConnect.company.entity;

import com.uniConnect.company.enums.IndustryType;
import jakarta.persistence.*;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "industries")
//업종: 소프트웨어 개발업, it서비스업
public class Industry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "industry_id")
    private Long industryId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 100)
    private IndustryType type;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    public static Industry of(IndustryType type) {
        return Industry.builder()
                .type(type)
                .name(type.getDisplayName())
                .build();
    }

}
