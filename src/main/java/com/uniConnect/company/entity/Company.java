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
    @Builder.Default //build시 field값초기화 유지
    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL)
    private List<User> users= new ArrayList<>();;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "industry_id")
    private Industry industry;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_type_id")
    private BusinessType businessType;

    // @PostLoad: DB에서 조회할 때 null 체크
    @PostLoad
    public void postLoad() {
        if (this.users == null) {
            this.users = new ArrayList<>();
        }
    }

    public boolean hasUser(Long userId) {
        if (userId == null) return false;
        if (this.users == null) return false;
        return users.stream().anyMatch(u -> u.getUserId().equals(userId));
    }

    // users 추가 헬퍼 메서드
    public void addUser(User user) {
        if (this.users == null) {
            this.users = new ArrayList<>();
        }
        user.setCompany(this);
        this.users.add(user);
    }

    // users getter 오버라이드 (null 방어)
    public List<User> getUsers() {
        if (this.users == null) {
            this.users = new ArrayList<>();
        }
        return this.users;
    }
}
