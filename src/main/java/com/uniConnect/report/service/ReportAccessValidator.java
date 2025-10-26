package com.uniConnect.report.service;

import com.uniConnect.member.entity.User;
import com.uniConnect.report.entity.SamplingReport;
import com.uniConnect.report.repository.SamplingReportRepository;
import com.uniConnect.campaign.entity.Campaign;
import com.uniConnect.studentOrg.entity.StudentOrg;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * ReportAccessValidator
 * - 현재 로그인한 유저가 해당 reportId 리포트를 조회할 수 있는지 검사
 * - 구조: report → campaign → studentOrg → user
 */
@Component
@RequiredArgsConstructor
public class ReportAccessValidator {

    private final SamplingReportRepository samplingReportRepository;

    public void validateCanAccessReport(Long reportId) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof User)) {
            throw new SecurityException("인증되지 않은 사용자입니다.");
        }
        User loginUser = (User) auth.getPrincipal();
        Long loginUserId = loginUser.getUserId();

        SamplingReport report = samplingReportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("리포트를 찾을 수 없습니다. id=" + reportId));

        Campaign campaign = report.getCampaign();
        if (campaign == null) {
            throw new IllegalStateException("해당 리포트에 연결된 캠페인이 없습니다.");
        }

        StudentOrg org = campaign.getStudentOrg();
        if (org == null || org.getUser() == null) {
            throw new IllegalStateException("해당 캠페인에 연결된 단체 또는 사용자 정보가 없습니다.");
        }

        Long ownerId = org.getUser().getUserId();

        if (!ownerId.equals(loginUserId)) {
            throw new SecurityException("이 리포트에 접근할 권한이 없습니다.");
        }
    }
}
