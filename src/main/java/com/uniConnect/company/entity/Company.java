package com.uniConnect.company.entity;

import com.uniConnect.common.entity.BaseEntity;
import com.uniConnect.member.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDate;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "companies")
public class Company extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "company_id")
    private Long companyId;

    // info
    @Column(name = "brand_name", length = 100)
    private String brandName;

    @Column(name = "logo_url", columnDefinition = "text")
    private String logoUrl;

    @Column(name = "main_contact_id")
    private Long mainContactId;

    @Column(name = "sampling_purpose", columnDefinition = "text")
    private String samplingPurpose;

    @Column(name = "sampling_start_date")
    private LocalDate samplingStartDate;

    @Column(name = "sampling_end_date")
    private LocalDate samplingEndDate;

    @Column(name = "product_name", length = 100)
    private String productName;

    @Column(name = "product_count")
    private Integer productCount;

    // relations
    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL)
    private List<User> users = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "industry_id")
    private Industry industry;

    public boolean hasUser(Long userId) {
        return users.stream().anyMatch(u -> u.getUserId().equals(userId));
    }
}
