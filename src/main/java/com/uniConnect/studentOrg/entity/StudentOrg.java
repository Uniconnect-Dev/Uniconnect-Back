package com.uniConnect.studentOrg.entity;

import com.uniConnect.member.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "student_orgs")
public class StudentOrg {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "student_org_id")
    private Long studentOrgId;

    // info
    @Column(name = "school_name", length = 100)
    private String schoolName;

    @Column(name = "organization_name", length = 120)
    private String organizationName;

    @Column(name = "manager_name", length = 60)
    private String managerName;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "email", length = 120)
    private String email;

    @Column(name = "logo_url", columnDefinition = "text")
    private String logoUrl;

    @Column(name = "verification_level")
    private Integer verificationLevel;

    @Column(name = "safety_flag")
    private Boolean safetyFlag;

    // relation
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "studentOrg", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudentOrgKeyword> keywords;

    @OneToMany(mappedBy = "studentOrg", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudentOrgAvailability> availabilities;
}
