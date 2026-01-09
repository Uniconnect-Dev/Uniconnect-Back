package com.uniConnect.partnership.controller;

import com.uniConnect.global.response.ApiResponse;
import com.uniConnect.member.security.local.CustomUser;
import com.uniConnect.partnership.dto.CollaborationProposalCreateRequest;
import com.uniConnect.partnership.service.CollaborationProposalService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/partnership/proposals")
@Tag(name = "Collaboration Proposal", description = "기업 협업 제안 API")
public class CollaborationProposalController {

    private final CollaborationProposalService proposalService;

    @PostMapping
    @Operation(
            summary = "1페이지: 기업 협업 제안 생성",
            description = """
        기업이 학생 단체에 협업 제안을 생성합니다.

        [제휴 유형 (proposalType)]
        - Discount : 학생 대상 할인 제휴
        - Etc : 기타 제휴 유형

        [협업 기간 유형 (periodType)]
        - Always : 상시 협업 (종료일 없음, startDate/endDate 미입력)
        - Fixed : 기간 지정 협업 (startDate, endDate 필수)

        ※ periodType이 Fixed인 경우 startDate와 endDate는 반드시 입력해야 합니다.
        """
    )
    public ApiResponse<Long> createProposal(
            @AuthenticationPrincipal CustomUser user,
            @RequestBody CollaborationProposalCreateRequest request
    ) {
        if (user == null || user.getUserId() == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }

        Long proposalId = proposalService.createProposal(
                user.getUserId(),
                request
        );

        return ApiResponse.success("협업 제안 생성 완료", proposalId);
    }

    @PostMapping("/{proposalId}/submit")
    @Operation(summary = "(약관 동의 후) 기업 협업 제안 최종 제출")
    public ApiResponse<Void> submitProposal(
            @AuthenticationPrincipal CustomUser user,
            @PathVariable Long proposalId
    ) {
        if (user == null || user.getUserId() == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }

        proposalService.submitProposal(user.getUserId(), proposalId);

        return ApiResponse.success("협업 제안 제출 완료", null);
    }

    @PostMapping(
            value = "/{proposalId}/attachment",
            consumes = "multipart/form-data"
    )
    @Operation(summary = "기업 협업 제안서 파일 업로드 (S3)")
    public ApiResponse<String> uploadProposalFile(
            @AuthenticationPrincipal CustomUser user,
            @PathVariable Long proposalId,
            @RequestPart("file") MultipartFile file
    ) {
        if (user == null || user.getUserId() == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 없습니다.");
        }

        String url = proposalService.uploadProposalFile(
                user.getUserId(),
                proposalId,
                file
        );

        return ApiResponse.success("협업 제안서 업로드 완료", url);
    }
}
