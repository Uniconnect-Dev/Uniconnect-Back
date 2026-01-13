package com.uniConnect.company.entity;

import com.uniConnect.company.enums.BusinessTypeEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
//업태: 법인사업자, 개인사업자
@Table(name = "business_types")
public class BusinessType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "business_type_id")
    private Long businessTypeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, unique = true)
    private BusinessTypeEnum type;

    @Column(name = "name", length = 100, nullable = false, unique = true)
    private String name;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public static BusinessType of(BusinessTypeEnum type) {
        return BusinessType.builder()
                .type(type)
                .name(type.getDisplayName())
                .build();
    }

//    public static BusinessType of(String name, String description) {
//        return BusinessType.builder()
//                .name(name)
//                .description(description)
//                .build();
//    }
}