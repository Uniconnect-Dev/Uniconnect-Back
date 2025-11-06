package com.uniConnect.report.service;

import com.uniConnect.member.security.local.CustomUser;
import com.uniConnect.report.entity.SamplingReport;
import com.uniConnect.report.repository.SamplingReportRepository;
import com.uniConnect.campaign.entity.Campaign;
import com.uniConnect.studentOrg.entity.StudentOrg;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReportAccessValidator {

    private final SamplingReportRepository samplingReportRepository;

    public void validateCanAccessReport(Long reportId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !(auth.getPrincipal() instanceof CustomUser user)) {
            throw new SecurityException("인증되지 않은 사용자입니다.");
        }

        Long loginUserId = user.getUserId();

        SamplingReport report = samplingReportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("리포트를 찾을 수 없습니다. id=" + reportId));

        Campaign campaign = report.getCampaign();
        if (campaign == null || campaign.getStudentOrg() == null) {
            throw new IllegalStateException("리포트에 연결된 단체 정보를 찾을 수 없습니다.");
        }

        StudentOrg org = campaign.getStudentOrg();
        Long ownerId = org.getUser().getUserId();

        if (!ownerId.equals(loginUserId)) {
            throw new SecurityException("이 리포트에 접근할 권한이 없습니다.");
        }
    }
}
