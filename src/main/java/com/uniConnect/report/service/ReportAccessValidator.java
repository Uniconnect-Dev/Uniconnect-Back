package com.uniConnect.report.service;

import com.uniConnect.member.security.local.CustomUser;
import com.uniConnect.report.entity.SamplingReport;
import com.uniConnect.report.repository.SamplingReportRepository;
import com.uniConnect.campaign.entity.Campaign;
import com.uniConnect.member.entity.LocalCredential;
import com.uniConnect.member.repository.LocalCredentialRepository;
import com.uniConnect.studentOrg.entity.StudentOrg;
import com.uniConnect.member.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReportAccessValidator {

    private final SamplingReportRepository samplingReportRepository;
    private final LocalCredentialRepository localCredentialRepository;

    public void validateCanAccessReport(Long reportId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null) {
            throw new SecurityException("인증되지 않은 사용자입니다.");
        }

        Object principal = auth.getPrincipal();
        Long loginUserId;

        // principal = loginId (String) 형태일 때
        if (principal instanceof String loginIdString) {

            LocalCredential credential = localCredentialRepository
                    .findByLoginId(loginIdString)
                    .orElseThrow(() -> new SecurityException("사용자 정보를 찾을 수 없습니다."));

            loginUserId = credential.getUser().getUserId();
        }
        // principal = CustomUser일 때
        else if (principal instanceof CustomUser customUser) {
            loginUserId = customUser.getUsersId();
        }
        else {
            throw new SecurityException("인증되지 않은 사용자입니다.");
        }

        // 리포트 조회
        SamplingReport report = samplingReportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("리포트를 찾을 수 없습니다. id=" + reportId));

        Campaign campaign = report.getCampaign();
        if (campaign == null || campaign.getStudentOrg() == null) {
            throw new IllegalStateException("리포트에 연결된 단체 정보를 찾을 수 없습니다.");
        }

        // student_org → userId
        Long ownerId = campaign.getStudentOrg().getUsers().isEmpty()
                ? null
                : campaign.getStudentOrg().getUsers().get(0).getUserId();

        if (!loginUserId.equals(ownerId)) {
            throw new SecurityException("이 리포트에 접근할 권한이 없습니다.");
        }
    }
}
