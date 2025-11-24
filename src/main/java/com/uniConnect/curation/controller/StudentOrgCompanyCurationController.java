package com.uniConnect.curation.controller;

import com.uniConnect.curation.dto.StudentOrgCompanyCurationRequest;
import com.uniConnect.curation.dto.CompanyCurationResponse;
import com.uniConnect.curation.service.StudentOrgCompanyCurationService;
import com.uniConnect.member.security.local.CustomUser;
import com.uniConnect.studentOrg.entity.StudentOrg;
import com.uniConnect.studentOrg.repository.StudentOrgRepository;
import com.uniConnect.global.response.ApiResponse;
import com.uniConnect.global.exception.CustomException;
import com.uniConnect.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/curation/student-org")
public class StudentOrgCompanyCurationController {

    private final StudentOrgCompanyCurationService curationService;
    private final StudentOrgRepository studentOrgRepository;

    @PostMapping("/companies")
    public ApiResponse<List<CompanyCurationResponse>> curate(
            @AuthenticationPrincipal CustomUser user,
            @RequestBody StudentOrgCompanyCurationRequest request
    ) {

        if (user == null || user.getUserId() == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        Long userId = user.getUserId();

        StudentOrg org = studentOrgRepository.findByUsers_UserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROFILE_NOT_FOUND));

        Long studentOrgId = org.getStudentOrgId();

        List<CompanyCurationResponse> result =
                curationService.curate(studentOrgId, request);

        return ApiResponse.success(result);
    }
}