package com.uniConnect.studentOrg.entity;

import com.uniConnect.member.entity.User;

import com.uniConnect.studentOrg.enums.CollaborationType;
import com.uniConnect.studentOrg.enums.OrganizationType;
import com.uniConnect.global.converter.EnumPascalCaseConverter;
import jakarta.persistence.*;
import lombok.*;
import java.util.*;

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

    @Convert(converter = EnumPascalCaseConverter.class)
    @Column(name = "organization_type", length = 30)
    private OrganizationType organizationType;

    @Convert(converter = EnumPascalCaseConverter.class)
    @Column(name = "collaboration_type", length = 30)
    private CollaborationType collaborationType;

    // relation
    @OneToMany(mappedBy = "studentOrg", cascade = CascadeType.ALL)
    private List<User> users = new ArrayList<>();

    @OneToMany(mappedBy = "studentOrg", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudentOrgKeyword> keywords;

    @OneToMany(mappedBy = "studentOrg", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudentOrgAvailability> availabilities;

    @OneToMany(mappedBy = "studentOrg", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudentOrgContact> contacts;

    @OneToMany(mappedBy = "studentOrg", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudentOrgHistory> histories;
}