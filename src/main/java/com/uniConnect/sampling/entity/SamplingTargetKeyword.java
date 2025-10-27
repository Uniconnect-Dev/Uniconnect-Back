package com.uniConnect.sampling.entity;

import com.uniConnect.sampling.enums.SamplingTargetCategory;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sampling_target_keyword")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SamplingTargetKeyword {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "target_keyword_id")
    private Long targetKeywordId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SamplingTargetCategory category;

    @Column(nullable = false, length = 100)
    private String label;

    @Column(length = 255)
    private String description;

    @Column(nullable = false)
    private boolean isActive;
}
