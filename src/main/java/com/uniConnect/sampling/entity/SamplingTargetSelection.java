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
    @JoinColumn(name = "sampling_request_id", nullable = false)
    private SamplingRequest samplingRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_keyword_id", nullable = false)
    private SamplingTargetKeyword targetKeyword;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SamplingTargetCategory category;

    @Column(name = "selected_label", length = 100)
    private String selectedLabel; // 선택된 키워드의 표시 이름(예: #이화여대)

    public static SamplingTargetSelection of(SamplingRequest request, SamplingTargetKeyword keyword) {
        return SamplingTargetSelection.builder()
                .samplingRequest(request)
                .targetKeyword(keyword)
                .category(keyword.getCategory())
                .selectedLabel(keyword.getLabel())
                .build();
    }
}
