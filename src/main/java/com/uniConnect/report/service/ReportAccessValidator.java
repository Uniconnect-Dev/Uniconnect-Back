package com.uniConnect.report.service;

import com.uniConnect.member.security.local.CustomUser;
import com.uniConnect.report.entity.SamplingReport;
import com.uniConnect.report.repository.SamplingReportRepository;
import com.uniConnect.campaign.entity.Campaign;
import com.uniConnect.member.entity.User;
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
    private final UserRepository userRepository;

    public void validateCanAccessReport(Long reportId) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) throw new SecurityException("인증되지 않은 사용자입니다.");

        Object principal = auth.getPrincipal();
        Long loginUserId;

        if (principal instanceof CustomUser customUser) {
            loginUserId = customUser.getUserId();
        } else if (principal instanceof String loginId) {
            // localCredential login 방식
            User user = userRepository.findByUsername(loginId)
                    .orElseThrow(() -> new SecurityException("사용자를 찾을 수 없습니다."));
            loginUserId = user.getUserId();
        } else {
            throw new SecurityException("인증되지 않은 사용자입니다.");
        }

        // 로그인 유저 조회
        User loginUser = userRepository.findById(loginUserId)
                .orElseThrow(() -> new SecurityException("사용자를 찾을 수 없습니다."));

        // 기업 소속 회사 ID
        Long userCompanyId = loginUser.getCompany() != null
                ? loginUser.getCompany().getCompanyId()
                : null;

        if (userCompanyId == null) {
            throw new SecurityException("기업 계정만 리포트에 접근할 수 있습니다.");
        }

        // 리포트 조회
        SamplingReport report = samplingReportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("리포트를 찾을 수 없습니다. id=" + reportId));

        Long reportCompanyId =
                report.getCollaboration().getMatchRequest()
                        .getCompany()
                        .getCompanyId();

        // 최종 권한 체크
        if (!userCompanyId.equals(reportCompanyId)) {
            throw new SecurityException("이 리포트에 접근할 권한이 없습니다.");
        }
    }
}
