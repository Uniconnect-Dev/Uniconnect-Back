package com.uniConnect.matching.controller;

import com.uniConnect.global.response.ApiResponse;
import com.uniConnect.matching.dto.MatchingCompleteRequest;
import com.uniConnect.matching.dto.MatchingCompleteResponse;
import com.uniConnect.matching.service.CollaborationMatchService;
import com.uniConnect.matching.service.CollaborationMatchQueryService;
import com.uniConnect.member.security.local.CustomUser;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/matching")
@Tag(name = "Collaboration Matching", description = "학생단체 ↔ 기업 매칭 요청 API")
public class CollaborationMatchController {

    private final CollaborationMatchService collaborationMatchService;
    private final CollaborationMatchQueryService collaborationMatchQueryService;

    @Operation(
            summary = "매칭 요청 마무리",
            description = "학생단체가 선택한 기업들에게 협업 매칭 요청을 전송합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "매칭 요청 생성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패 또는 토큰 누락"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/complete")
    public ApiResponse<MatchingCompleteResponse> completeMatching(
            @AuthenticationPrincipal CustomUser user,
            @RequestBody MatchingCompleteRequest dto
    ) {
        if (user == null || user.getUserId() == null) {
            throw new IllegalStateException("인증 정보가 없습니다. JWT 토큰을 확인하세요.");
        }

        Long studentOrgId = collaborationMatchQueryService.findStudentOrgIdByUserId(user.getUserId());

        return ApiResponse.success(
                collaborationMatchService.completeMatching(studentOrgId, dto)
        );
    }
}