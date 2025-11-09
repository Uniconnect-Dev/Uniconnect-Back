package com.uniConnect.studentOrg.controller;

import com.uniConnect.global.response.ApiResponse;
import com.uniConnect.studentOrg.dto.StudentOrgCalendarResponse;
import com.uniConnect.studentOrg.dto.StudentOrgProfileDetailResponse;
import com.uniConnect.studentOrg.dto.StudentOrgProfileListResponse;
import com.uniConnect.studentOrg.enums.CollaborationType;
import com.uniConnect.studentOrg.enums.OrganizationType;
import com.uniConnect.studentOrg.service.StudentOrgProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Student Org Profile API", description = "기업이 학생 단체 프로필을 확인하는 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/student-orgs/profiles")
public class StudentOrgProfileController {

    private final StudentOrgProfileService profileService;

    @Operation(summary = "학생 단체 프로필 리스트 조회", description = "검색/필터를 통해 유니커넥트와 협업한 학생단체를 조회합니다.")
    @GetMapping
    public ApiResponse<List<StudentOrgProfileListResponse>> getProfiles(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) CollaborationType collabType,
            @RequestParam(required = false) OrganizationType orgType
    ) {
        return ApiResponse.success(profileService.getProfileList(keyword, collabType, orgType));
    }

    @Operation(summary = "학생 단체 프로필 상세 조회", description = "학생 단체가 등록한 행사 정보, 참여자 특성, 유니커넥트 협업 이력을 확인합니다.")
    @GetMapping("/{studentOrgId}")
    public ApiResponse<StudentOrgProfileDetailResponse> getProfileDetail(
            @PathVariable Long studentOrgId
    ) {
        return ApiResponse.success(profileService.getProfileDetail(studentOrgId));
    }

    @Operation(summary = "학생 단체 샘플링 일정 캘린더", description = "현재 월 기준 -1개월 ~ +3개월 일정만 반환합니다.")
    @GetMapping("/{studentOrgId}/calendar")
    public ApiResponse<List<StudentOrgCalendarResponse>> getCalendar(
            @PathVariable Long studentOrgId
    ) {
        return ApiResponse.success(profileService.getCalendar(studentOrgId));
    }
}