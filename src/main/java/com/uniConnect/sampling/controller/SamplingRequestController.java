package com.uniConnect.sampling.controller;

import com.uniConnect.global.response.ApiResponse;
import com.uniConnect.member.security.local.CustomUser;
import com.uniConnect.member.repository.UserRepository;
import com.uniConnect.sampling.dto.request.*;
import com.uniConnect.sampling.dto.response.*;
import com.uniConnect.member.entity.User;
import com.uniConnect.member.entity.LocalCredential;
import com.uniConnect.member.repository.LocalCredentialRepository;
import com.uniConnect.member.security.local.JwtUtil;
import com.uniConnect.sampling.dto.response.SamplingRequestSummaryResponse;
import com.uniConnect.sampling.entity.SamplingRequest;
import com.uniConnect.sampling.service.SamplingRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Sampling Request API", description = "학생 단체 샘플링 요청 생성 및 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sampling/requests")
public class SamplingRequestController {

    private final SamplingRequestService requestService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final LocalCredentialRepository localCredentialRepository;

    /** Step 0 */
    @PostMapping("/draft")
    @Operation(summary = "샘플링 초안 생성", description = "학생단체 샘플링 초안을 생성합니다.")
    public ApiResponse<SamplingRequestSummaryResponse> createDraft(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("인증 토큰이 없습니다. Authorization: Bearer ... 헤더를 넣어주세요.");
        }

        String token = authHeader.substring(7);
        String loginId = jwtUtil.extractSubject(token);

        var credential = localCredentialRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("로그인 아이디에 해당하는 사용자를 찾을 수 없습니다: " + loginId));

        User user = credential.getUser();
        SamplingRequest saved = requestService.createDraft(user.getUserId());

        SamplingRequestSummaryResponse summary = requestService.getSummary(saved.getSamplingRequestId());
        return ApiResponse.success("초안 생성 완료", summary);
    }

    /** Step 1 */
    @Operation(summary = "Step 1: 단체 기본정보 입력", description = "학교명, 단체명, 담당자명, 전화번호, 이메일을 등록 또는 수정합니다.")
    @PutMapping("/{id}/org-info")
    public ApiResponse<SamplingRequestSummaryResponse> step1(
            @PathVariable Long id,
            @Valid @RequestBody OrgInfoRequest dto
    ) {

        requestService.updateStep1(id, dto);

        SamplingRequestSummaryResponse summary = requestService.getSummary(id);
        return ApiResponse.success("단체 기본정보 저장 완료", summary);
    }

    /** Step 2 */
    @Operation(summary = "Step 2: 단체 해시태그 선택", description = "카테고리별 단체 해시태그를 선택합니다. (해시태그 각 최대 5개)")
    @PutMapping("/{id}/tags")
    public ApiResponse<SamplingRequestSummaryResponse> step2(@PathVariable Long id, @Valid @RequestBody TargetTagRequest dto) {
        requestService.updateStep2(id, dto);
        SamplingRequestSummaryResponse summary = requestService.getSummary(id);
        return ApiResponse.success("단체 해시태그 저장 완료", summary);
    }

    /** Step 3 */
    @Operation(summary = "Step 3: 행사 정보 입력", description = "행사명, 행사 설명, 필요 개수, 기간, 이벤트 관련 해시태그를 입력합니다.")
    @PutMapping("/{id}/event")
    public ApiResponse<SamplingRequestSummaryResponse> step3(@PathVariable Long id, @Valid @RequestBody EventInfoRequest dto) {
        requestService.updateStep3(id, dto);
        SamplingRequestSummaryResponse summary = requestService.getSummary(id);
        return ApiResponse.success("행사 정보 저장 완료", summary);
    }

    /** Step 4 */
    @Operation(summary = "Step 4: 산업군 및 공식 해시태그 선택", description = "희망 산업군과 관련된 공식 해시태그를 선택합니다.")
    @PutMapping("/{id}/industry")
    public ApiResponse<SamplingRequestSummaryResponse> step4(@PathVariable Long id, @Valid @RequestBody IndustryRequest dto) {
        requestService.updateStep4(id, dto);
        SamplingRequestSummaryResponse summary = requestService.getSummary(id);
        return ApiResponse.success("산업군 정보 저장 완료", summary);
    }

    /** Step 5 */
    @Operation(
            summary = "Step 5: 제안서 업로드",
            description = "학생단체가 샘플링 제안서를 S3에 업로드하고, 업로드 완료 후 요청 상태를 Submitted로 변경합니다."
    )
    @PostMapping(value = "/{id}/proposal", consumes = "multipart/form-data")
    public ApiResponse<SamplingRequestSummaryResponse> uploadProposal(
            @PathVariable Long id,
            @Parameter(
                    description = "제안서 파일 (PDF, DOCX 등)",
                    required = true,
                    content = @Content(
                            mediaType = "multipart/form-data",
                            schema = @Schema(type = "string", format = "binary")
                    )
            )
            @RequestPart("file") MultipartFile file
    ) throws Exception {

        requestService.uploadProposal(id, file);
        SamplingRequestSummaryResponse summary = requestService.getSummary(id);
        return ApiResponse.success("제안서 업로드 완료", summary);
    }

    @Operation(
            summary = "Step 6: 요청 승인 (관리자)",
            description = "관리자가 샘플링 요청을 승인합니다. 승인 시 상태는 Approved로 변경됩니다."
    )
    @PostMapping("/{id}/approve")
    public ApiResponse<SamplingRequestSummaryResponse> approveRequest(@PathVariable Long id) {
        requestService.approveRequest(id);
        SamplingRequestSummaryResponse summary = requestService.getSummary(id);
        return ApiResponse.success("승인 완료", summary);
    }

    @Operation(
            summary = "Step 6-1: 요청 반려 (관리자)",
            description = "관리자가 샘플링 요청을 반려합니다. 상태는 Rejected로 변경됩니다."
    )
    @PostMapping("/{id}/reject")
    public ApiResponse<SamplingRequestSummaryResponse> rejectRequest(@PathVariable Long id) {
        requestService.rejectRequest(id);
        SamplingRequestSummaryResponse summary = requestService.getSummary(id);
        return ApiResponse.success("반려 완료", summary);
    }

    /** 워크플로우 */
    @Operation(
            summary = "Step 7: 계약서 서명 (학생단체)",
            description = "학생단체가 계약서를 확인 후 전자 서명을 완료합니다. 상태는 ContractApprovalPending으로 변경됩니다."
    )
    @PostMapping("/{id}/contract/sign")
    public ApiResponse<SamplingRequestSummaryResponse> signContract(@PathVariable Long id) {
        requestService.signContract(id);
        SamplingRequestSummaryResponse summary = requestService.getSummary(id);
        return ApiResponse.success("계약서 서명 완료", summary);
    }

    @Operation(
            summary = "Step 8: 계약 승인 (관리자)",
            description = "관리자가 학생단체의 계약서 서명을 승인합니다. 상태는 ReceiptPending으로 변경됩니다."
    )
    @PostMapping("/{id}/contract/approve")
    public ApiResponse<SamplingRequestSummaryResponse> approveContract(@PathVariable Long id) {
        requestService.approveContract(id);
        SamplingRequestSummaryResponse summary = requestService.getSummary(id);
        return ApiResponse.success("계약 승인 완료", summary);
    }

    @Operation(
            summary = "Step 9: 인수증 업로드",
            description = "제품 수령 후 인수증을 업로드합니다."
    )
    @PostMapping(value = "/{id}/receipt", consumes = "multipart/form-data")
    public ApiResponse<SamplingRequestSummaryResponse> uploadReceipt(
            @PathVariable Long id,
            @Parameter(
                    description = "인수증 파일 (이미지, PDF 등)",
                    required = true,
                    content = @Content(
                            mediaType = "multipart/form-data",
                            schema = @Schema(type = "string", format = "binary")
                    )
            )
            @RequestPart("file") MultipartFile file
    ) throws Exception {

        requestService.uploadReceipt(id, file);
        SamplingRequestSummaryResponse summary = requestService.getSummary(id);
        return ApiResponse.success("인수증 업로드 완료", summary);
    }

    @Operation(
            summary = "Step 9-1: 인수증 승인 (관리자)",
            description = "관리자가 인수증을 승인하여 상태를 ReportPending으로 변경합니다."
    )
    @PostMapping("/{id}/receipt/approve")
    public ApiResponse<SamplingRequestSummaryResponse> approveReceipt(@PathVariable Long id) {
        requestService.approveReceipt(id);
        SamplingRequestSummaryResponse summary = requestService.getSummary(id);
        return ApiResponse.success("인수증 승인 완료", summary);
    }

    @Operation(
            summary = "Step 10: 리포트 업로드",
            description = "행사 종료 후 리포트를 업로드합니다."
    )
    @PostMapping(value = "/{id}/report", consumes = "multipart/form-data")
    public ApiResponse<SamplingRequestSummaryResponse> uploadReport(
            @PathVariable Long id,
            @Parameter(
                    description = "리포트 파일 (PDF 등)",
                    required = true,
                    content = @Content(
                            mediaType = "multipart/form-data",
                            schema = @Schema(type = "string", format = "binary")
                    )
            )
            @RequestPart("file") MultipartFile file
    ) throws Exception {

        requestService.uploadReport(id, file);
        SamplingRequestSummaryResponse summary = requestService.getSummary(id);
        return ApiResponse.success("리포트 업로드 완료", summary);
    }

    @Operation(
            summary = "Step 10-1: 리포트 승인 (관리자)",
            description = "관리자가 리포트를 승인하여 상태를 SurveyPending으로 변경합니다."
    )
    @PostMapping("/{id}/report/approve")
    public ApiResponse<SamplingRequestSummaryResponse> approveReport(@PathVariable Long id) {
        requestService.approveReport(id);
        SamplingRequestSummaryResponse summary = requestService.getSummary(id);
        return ApiResponse.success("리포트 승인 완료", summary);
    }

    @Operation(
            summary = "Step 11: 설문 완료",
            description = "모든 협업 절차 완료 후 학생단체가 설문을 완료 처리합니다. 상태는 Completed로 변경됩니다."
    )
    @PostMapping("/{id}/survey/complete")
    public ApiResponse<SamplingRequestSummaryResponse> completeSurvey(@PathVariable Long id) {
        requestService.completeSurvey(id);
        SamplingRequestSummaryResponse summary = requestService.getSummary(id);
        return ApiResponse.success("설문 완료", summary);
    }

    @Operation(summary = "샘플링 요청 제출", description = "입력된 모든 정보를 검증하고 요청 상태를 Submitted로 변경합니다.")
    @PostMapping("/{id}/submit")
    public ApiResponse<SamplingRequestSummaryResponse> submit(@PathVariable Long id) {
        requestService.submit(id);
        SamplingRequestSummaryResponse summary = requestService.getSummary(id);
        return ApiResponse.success("샘플링 요청 제출 완료", summary);
    }

    @Operation(summary = "샘플링 요청 요약 조회", description = "입력된 모든 정보를 종합하여 요약 정보를 반환합니다.")
    @GetMapping("/{id}/summary")
    public ApiResponse<SamplingRequestSummaryResponse> summary(@PathVariable Long id) {
        SamplingRequestSummaryResponse summary = requestService.getSummary(id);
        return ApiResponse.success("요약 조회 성공", summary);
    }
}