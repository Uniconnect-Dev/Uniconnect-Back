package com.uniConnect.sampling.controller;

import com.uniConnect.sampling.dto.request.*;
import com.uniConnect.sampling.dto.response.SamplingRequestSummaryResponse;
import com.uniConnect.sampling.service.SamplingRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Sampling Request API", description = "학생 단체 샘플링 요청 생성 및 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sampling/requests")
public class SamplingRequestController {

    private final SamplingRequestService requestService;

    @Operation(summary = "샘플링 요청 초안 생성", description = "새로운 샘플링 요청 초안을 생성하고, 생성된 요청 ID를 반환합니다.")
    @PostMapping("/draft")
    public ResponseEntity<Long> createDraft() {
        return ResponseEntity.ok(requestService.createDraft());
    }

    @Operation(summary = "Step 1: 단체 기본정보 입력", description = "학교명, 단체명, 담당자명, 전화번호, 이메일을 등록 또는 수정합니다.")
    @PutMapping("/{id}/org-info")
    public ResponseEntity<Void> step1(@PathVariable Long id, @Valid @RequestBody OrgInfoRequest dto) {
        requestService.updateStep1(id, dto);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Step 2: 단체 해시태그 선택", description = "카테고리별 단체 해시태그를 선택합니다. (해시태그 각 최대 5개)")
    @PutMapping("/{id}/tags")
    public ResponseEntity<Void> step2(@PathVariable Long id, @Valid @RequestBody TargetTagRequest dto) {
        requestService.updateStep2(id, dto);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Step 3: 행사 정보 입력", description = "행사명, 행사 설명, 필요 개수, 기간, 이벤트 관련 해시태그를 입력합니다.")
    @PutMapping("/{id}/event")
    public ResponseEntity<Void> step3(@PathVariable Long id, @Valid @RequestBody EventInfoRequest dto) {
        requestService.updateStep3(id, dto);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Step 4: 산업군 및 공식 해시태그 선택", description = "희망 산업군과 관련된 공식 해시태그를 선택합니다.")
    @PutMapping("/{id}/industry")
    public ResponseEntity<Void> step4(@PathVariable Long id, @Valid @RequestBody IndustryRequest dto) {
        requestService.updateStep4(id, dto);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "제안서(PDF) 업로드", description = "S3 버킷에 제안서를 업로드하고 업로드된 파일의 URL을 반환합니다.")
    @PostMapping("/{id}/proposal")
    public ResponseEntity<String> uploadProposal(@PathVariable Long id, @RequestParam("file") MultipartFile file) throws Exception {
        return ResponseEntity.ok(requestService.uploadProposal(id, file));
    }

    @Operation(summary = "샘플링 요청 제출", description = "입력된 모든 정보를 검증하고 요청 상태를 Submitted로 변경합니다.")
    @PostMapping("/{id}/submit")
    public ResponseEntity<Void> submit(@PathVariable Long id) {
        requestService.submit(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "샘플링 요청 요약 조회", description = "입력된 모든 정보를 종합하여 요약 정보를 반환합니다.")
    @GetMapping("/{id}/summary")
    public ResponseEntity<SamplingRequestSummaryResponse> summary(@PathVariable Long id) {
        return ResponseEntity.ok(requestService.getSummary(id));
    }
}
