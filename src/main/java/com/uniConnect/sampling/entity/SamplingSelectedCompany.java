package com.uniConnect.sampling.entity;

import com.uniConnect.company.entity.Company;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "sampling_selected_company")
public class SamplingSelectedCompany {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 샘플링 요청 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sampling_request_id", nullable = false)
    private SamplingRequest samplingRequest;

    /** 학생단체가 선택한 기업 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    public static SamplingSelectedCompany of(SamplingRequest req, Company company) {
        return SamplingSelectedCompany.builder()
                .samplingRequest(req)
                .company(company)
                .build();
    }
}
