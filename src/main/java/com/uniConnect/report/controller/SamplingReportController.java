package com.uniConnect.report.controller;

import com.uniConnect.global.response.ApiResponse;
import com.uniConnect.report.dto.request.SamplingReportRequest;
import com.uniConnect.report.entity.SamplingReport;
import com.uniConnect.member.entity.User;
import com.uniConnect.report.service.SamplingReportService;
import com.uniConnect.member.repository.LocalCredentialRepository;
import com.uniConnect.member.repository.UserRepository;
import com.uniConnect.member.security.local.JwtUtil;
import com.uniConnect.studentOrg.entity.StudentOrg;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;


import java.util.*;

@Tag(name = "Sampling Report API", description = "학생단체 리포트 제출 및 관리 API")
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Slf4j
public class SamplingReportController {

    private final SamplingReportService reportService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final LocalCredentialRepository localCredentialRepository;

    @Operation(
            summary = "학생단체 리포트 제출",
            description = "로그인한 학생단체 소속 사용자 계정 기반으로 리포트를 제출합니다. 사진/영상 첨부 가능."
    )
    @PostMapping(value = "/submit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<SamplingReport> submitReport(
            @RequestHeader(value = "Authorization") String authHeader,
            @ModelAttribute SamplingReportRequest request,
            @RequestPart(required = false) List<MultipartFile> mediaFiles
    ) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("인증 토큰이 없습니다. Authorization: Bearer ... 헤더를 넣어주세요.");
        }

        String token = authHeader.substring(7);
        String loginId = jwtUtil.extractSubject(token);

        var credential = localCredentialRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("로그인 아이디에 해당하는 사용자를 찾을 수 없습니다: " + loginId));

        User user = credential.getUser();

        StudentOrg studentOrg = user.getStudentOrg();
        if (studentOrg == null) {
            throw new IllegalArgumentException("이 사용자는 학생단체 소속이 아닙니다. 학생단체 계정으로 로그인 후 다시 시도하세요.");
        }

        SamplingReport savedReport = reportService.submitReport(request, mediaFiles, studentOrg.getStudentOrgId());
        return ApiResponse.success("리포트가 성공적으로 제출되었습니다.", savedReport);
    }

    @Operation(summary = "어드민 리포트 승인", description = "리포트 승인 시 캠페인 상태가 완료로 변경됩니다.")
    @PostMapping("/{reportId}/approve")
    public ApiResponse<String> approveReport(@PathVariable Long reportId) {
        reportService.approveReport(reportId);
        return ApiResponse.success("리포트가 승인되었습니다.", null);
    }
}