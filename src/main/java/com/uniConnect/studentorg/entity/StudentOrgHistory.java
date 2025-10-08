package com.uniConnect.studentorg.entity;

import com.uniConnect.studentorg.enums.OrgHistoryStatus;
import com.uniConnect.studentorg.enums.OrgRoleType;
import jakarta.persistence.*;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "student_org_histories")
public class StudentOrgHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;

    // info
    @Column(name = "campaign_id")
    private Long campaignId; // 실제 Campaign 연관으로 바꿀 수 있으나, ERD는 정수만 명시

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 20)
    private OrgRoleType role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private OrgHistoryStatus status;

    // relation
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_org_id")
    private StudentOrg studentOrg;
}
