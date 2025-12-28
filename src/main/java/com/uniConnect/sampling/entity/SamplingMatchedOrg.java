package com.uniConnect.sampling.entity;

import com.uniConnect.studentOrg.entity.StudentOrg;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SamplingMatchedOrg {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sampling_request_id")
    private SamplingRequest samplingRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_org_id")
    private StudentOrg studentOrg;

    public static SamplingMatchedOrg of(SamplingRequest req, StudentOrg org) {
        return SamplingMatchedOrg.builder()
                .samplingRequest(req)
                .studentOrg(org)
                .build();
    }
}
