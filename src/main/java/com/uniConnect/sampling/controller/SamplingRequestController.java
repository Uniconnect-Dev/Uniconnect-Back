package com.uniConnect.sampling.controller;

import com.uniConnect.global.response.ApiResponse;
import com.uniConnect.member.security.local.CustomUser;
import com.uniConnect.sampling.dto.request.*;
import com.uniConnect.sampling.dto.response.*;
import com.uniConnect.sampling.dto.FeeResponse;
import com.uniConnect.signature.dto.SignatureRequest;
import com.uniConnect.signature.service.SignatureService;
import com.uniConnect.global.exception.CustomException;
import com.uniConnect.global.exception.ErrorCode;
import com.uniConnect.sampling.entity.SamplingRequest;
import com.uniConnect.sampling.service.SamplingRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.annotation.AuthenticationPrincipal;


@Tag(name = "Sampling Request API", description = "학생 단체 샘플링 요청 생성 및 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sampling/requests")
public class SamplingRequestController {

    private final SamplingRequestService requestService;
    private final SignatureService signatureService;


    /* ============================================================
       공통 유틸
    ============================================================ */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty())
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);

        if (file.getSize() > 15 * 1024 * 1024)
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
    }

    private void requireOwner(Long userId, Long ownerId) {
        if (!userId.equals(ownerId))
            throw new CustomException(ErrorCode.ACCESS_DENIED);
    }

    /**
     * CustomUser 에 getRole()이 없으므로 권한은 authorities 에서 ROLE_Admin 체크
     */
    private boolean isAdmin(CustomUser user) {
        return user != null &&
                user.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_Admin"));
    }

    private void requireAdmin(CustomUser user) {
        if (!isAdmin(user))
            throw new CustomException(ErrorCode.ACCESS_DENIED);
    }


    /* ============================================================
       Step 0 — 초안 생성
    ============================================================ */
    @PostMapping("/draft")
    @Operation(summary = "Step 0: 샘플링 초안 생성")
    public ApiResponse<SamplingRequestSummaryResponse> createDraft(
            @AuthenticationPrincipal CustomUser user
    ) {
        if (user == null) throw new CustomException(ErrorCode.UNAUTHORIZED);

        SamplingRequest saved = requestService.createDraft(user.getUserId());

        return ApiResponse.success(
                "초안 생성 완료",
                requestService.getSummary(saved.getSamplingRequestId())
        );
    }


    /* ============================================================
       Step 1 — 단체 기본정보
    ============================================================ */
    @PutMapping("/{id}/org-info")
    @Operation(summary = "Step 1: 단체 기본정보 입력")
    public ApiResponse<SamplingRequestSummaryResponse> step1(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUser user,
            @Valid @RequestBody OrgInfoRequest dto
    ) {
        requireOwner(user.getUserId(), requestService.getOwnerId(id));
        requestService.updateStep1(id, dto);

        return ApiResponse.success(
                "단체 기본정보 저장 완료",
                requestService.getSummary(id)
        );
    }


    /* ============================================================
       Step 2 — 해시태그 선택
    ============================================================ */
    @PutMapping("/{id}/tags")
    @Operation(summary = "Step 2: 단체 해시태그 선택")
    public ApiResponse<SamplingRequestSummaryResponse> step2(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUser user,
            @Valid @RequestBody TargetTagRequest dto
    ) {
        requireOwner(user.getUserId(), requestService.getOwnerId(id));

        requestService.updateStep2(id, dto);

        return ApiResponse.success(
                "단체 해시태그 저장 완료",
                requestService.getSummary(id)
        );
    }


    /* ============================================================
       Step 3 — 행사 정보
    ============================================================ */
    @PutMapping("/{id}/event")
    @Operation(summary = "Step 3: 행사 정보 입력")
    public ApiResponse<SamplingRequestSummaryResponse> step3(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUser user,
            @Valid @RequestBody EventInfoRequest dto
    ) {
        requireOwner(user.getUserId(), requestService.getOwnerId(id));
        requestService.updateStep3(id, dto);

        return ApiResponse.success(
                "행사 정보 저장 완료",
                requestService.getSummary(id)
        );
    }


    /* ============================================================
       Step 4 — 산업군
    ============================================================ */
    @PutMapping("/{id}/industry")
    @Operation(summary = "Step 4: 산업군 정보 저장")
    public ApiResponse<SamplingRequestSummaryResponse> step4(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUser user,
            @Valid @RequestBody IndustryRequest dto
    ) {
        requireOwner(user.getUserId(), requestService.getOwnerId(id));
        requestService.updateStep4(id, dto);

        return ApiResponse.success(
                "산업군 정보 저장 완료",
                requestService.getSummary(id)
        );
    }


    /* ============================================================
       Step 5 — 제안서 업로드
    ============================================================ */
    @PostMapping(value = "/{id}/proposal", consumes = "multipart/form-data")
    @Operation(summary = "Step 5: 제안서 업로드")
    public ApiResponse<SamplingRequestSummaryResponse> uploadProposal(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUser user,
            @RequestPart("file") MultipartFile file
    ) throws Exception {

        requireOwner(user.getUserId(), requestService.getOwnerId(id));
        validateFile(file);

        requestService.uploadProposal(id, file);

        return ApiResponse.success(
                "제안서 업로드 완료",
                requestService.getSummary(id)
        );
    }


    /* ============================================================
       선택 기업 저장
    ============================================================ */
    @PostMapping("/{id}/companies")
    @Operation(summary = "선택 기업 저장")
    public ApiResponse<SamplingRequestSummaryResponse> saveSelectedCompanies(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUser user,
            @RequestBody CompanySelectRequest dto
    ) {
        requireOwner(user.getUserId(), requestService.getOwnerId(id));

        return ApiResponse.success(
                "선택 기업 저장 완료",
                requestService.saveSelectedCompanies(id, dto.companyIds())
        );
    }


    /* ============================================================
       관리자 승인 / 반려
    ============================================================ */
    @PostMapping("/{id}/approve")
    @Operation(summary = "요청 승인 (관리자)")
    public ApiResponse<SamplingRequestSummaryResponse> approveRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUser user
    ) {
        requireAdmin(user);
        requestService.approveRequest(id);

        return ApiResponse.success(
                "승인 완료",
                requestService.getSummary(id)
        );
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "요청 반려 (관리자)")
    public ApiResponse<SamplingRequestSummaryResponse> rejectRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUser user
    ) {
        requireAdmin(user);
        requestService.rejectRequest(id);

        return ApiResponse.success(
                "반려 완료",
                requestService.getSummary(id)
        );
    }


    /* ============================================================
       계약서 서명
    ============================================================ */
    @PostMapping("/{id}/contract/sign")
    @Operation(summary = "계약서 서명 (학생단체)")
    public ApiResponse<SamplingRequestSummaryResponse> signContract(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUser user,
            @Valid @RequestBody SignatureRequest signatureRequest
    ) {
        requireOwner(user.getUserId(), requestService.getOwnerId(id));

        signatureService.saveSignature(signatureRequest, user.getUserId());
        requestService.signContract(id, user.getUserId());

        return ApiResponse.success(
                "계약서 서명 완료",
                requestService.getSummary(id)
        );
    }


    /* ============================================================
       계약 승인 (관리자)
    ============================================================ */
    @PostMapping("/{id}/contract/approve")
    @Operation(summary = "계약 승인 (관리자)")
    public ApiResponse<SamplingRequestSummaryResponse> approveContract(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUser user
    ) {
        requireAdmin(user);
        requestService.approveContract(id);

        return ApiResponse.success(
                "계약 승인 완료",
                requestService.getSummary(id)
        );
    }


    /* ============================================================
       인수증 업로드
    ============================================================ */
    @PostMapping(value = "/{id}/receipt", consumes = "multipart/form-data")
    @Operation(summary = "인수증 업로드")
    public ApiResponse<SamplingRequestSummaryResponse> uploadReceipt(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUser user,
            @RequestPart("file") MultipartFile file
    ) throws Exception {

        requireOwner(user.getUserId(), requestService.getOwnerId(id));
        validateFile(file);

        requestService.uploadReceipt(id, file);

        return ApiResponse.success(
                "인수증 업로드 완료",
                requestService.getSummary(id)
        );
    }


    @PostMapping("/{id}/receipt/approve")
    @Operation(summary = "인수증 승인 (관리자)")
    public ApiResponse<SamplingRequestSummaryResponse> approveReceipt(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUser user
    ) {
        requireAdmin(user);

        requestService.approveReceipt(id);

        return ApiResponse.success(
                "인수증 승인 완료",
                requestService.getSummary(id)
        );
    }


    /* ============================================================
       리포트 업로드
    ============================================================ */
    @PostMapping(value = "/{id}/report", consumes = "multipart/form-data")
    @Operation(summary = "리포트 업로드")
    public ApiResponse<SamplingRequestSummaryResponse> uploadReport(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUser user,
            @RequestPart("file") MultipartFile file
    ) throws Exception {

        requireOwner(user.getUserId(), requestService.getOwnerId(id));
        validateFile(file);

        requestService.uploadReport(id, file);

        return ApiResponse.success(
                "리포트 업로드 완료",
                requestService.getSummary(id)
        );
    }


    @PostMapping("/{id}/report/approve")
    @Operation(summary = "리포트 승인 (관리자)")
    public ApiResponse<SamplingRequestSummaryResponse> approveReport(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUser user
    ) {
        requireAdmin(user);

        requestService.approveReport(id);

        return ApiResponse.success(
                "리포트 승인 완료",
                requestService.getSummary(id)
        );
    }


    /* ============================================================
       Survey 완료
    ============================================================ */
    @PostMapping("/{id}/survey/complete")
    @Operation(summary = "설문 완료")
    public ApiResponse<SamplingRequestSummaryResponse> completeSurvey(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUser user
    ) {
        requireOwner(user.getUserId(), requestService.getOwnerId(id));
        requestService.completeSurvey(id);

        return ApiResponse.success(
                "설문 완료",
                requestService.getSummary(id)
        );
    }


    /* ============================================================
       제출 & 요약
    ============================================================ */
    @PostMapping("/{id}/submit")
    @Operation(summary = "샘플링 요청 최종 제출")
    public ApiResponse<SamplingRequestSummaryResponse> submit(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUser user
    ) {
        requireOwner(user.getUserId(), requestService.getOwnerId(id));
        requestService.submit(id);

        return ApiResponse.success(
                "샘플링 요청 제출 완료",
                requestService.getSummary(id)
        );
    }

    @GetMapping("/{id}/summary")
    @Operation(summary = "요약 정보 조회")
    public ApiResponse<SamplingRequestSummaryResponse> summary(
            @PathVariable Long id
    ) {
        return ApiResponse.success(
                "요약 조회 성공",
                requestService.getSummary(id)
        );
    }


    /* ============================================================
       기타
    ============================================================ */
    @GetMapping("/fee")
    @Operation(summary = "샘플링 비용 조회")
    public ApiResponse<FeeResponse> getSamplingFees() {
        return ApiResponse.success(
                requestService.getFeeInformation()
        );
    }


    @PostMapping("/{id}/match/request")
    @Operation(summary = "학생단체 매칭 요청 제출")
    public ApiResponse<String> requestMatching(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUser user
    ) {
        requireOwner(user.getUserId(), requestService.getOwnerId(id));
        requestService.requestMatching(id);

        return ApiResponse.success(
                "UNI:CONNECT를 이용해주셔서 감사합니다. 담당자 확인 후 단체 페이지를 통해 매칭 여부 전달드리겠습니다. (평균 24시간 소요됩니다.)"
        );
    }
}
