package com.uniConnect.company.entity;

import com.uniConnect.common.entity.BaseEntity;
import com.uniConnect.member.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;
import java.util.ArrayList;

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
    private Long mainContactId; // 필요 시 연관관계로 교체 가능

    // relations
    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL)
    private List<User> users = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "industry_id")
    private Industry industry;
}
