package com.uniConnect.sampling.entity;

import com.uniConnect.member.entity.User;
import com.uniConnect.sampling.enums.SamplingTargetCategory;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sampling_target_selection")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SamplingTargetSelection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "selection_id")
    private Long selectionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SamplingTargetCategory category;

    @Column(name = "selected_keywords_csv", length = 1000)
    private String selectedKeywordsCsv;
}
