package com.uniConnect.collaboration.controller;

import com.uniConnect.collaboration.dto.SurveyCompleteRequest;
import com.uniConnect.collaboration.dto.CollaborationDetailResponse;
import com.uniConnect.collaboration.service.CollaborationWorkflowService;
import com.uniConnect.global.response.ApiResponse;
import com.uniConnect.s3.S3FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 학생단체용 협업 진행 API
 */
@Tag(name = "Collaboration Workflow API", description = "학생단체 협업 진행 API (계약 / 인수증 / 리포트 / 설문)")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/collaborations/student-org")
public class CollaborationWorkflowController {

    private final CollaborationWorkflowService service;

    @Operation(summary = "계약서 보기", description = "해당 협업의 계약서를 조회합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(schema = @Schema(implementation = CollaborationDetailResponse.class))
    )
    @GetMapping("/{id}/contract")
    public ApiResponse<CollaborationDetailResponse> getContract(
            @Parameter(description = "협업 ID", example = "1") @PathVariable Long id) {
        return ApiResponse.success(service.getContract(id));
    }

    @Operation(summary = "계약서 서명", description = "학생단체가 계약서에 전자 서명을 진행합니다.")
    @PostMapping("/{id}/contract/sign")
    public ApiResponse<Void> signContract(
            @Parameter(description = "협업 ID", example = "1") @PathVariable Long id) {
        service.signContract(id);
        return ApiResponse.success("계약서 서명이 완료되었습니다.", null);
    }

    @Operation(summary = "인수증 업로드", description = "제품 수령 후 인수증 파일을 업로드합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)))
    @PostMapping(value = "/{id}/receipt", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Void> uploadReceipt(
            @Parameter(description = "협업 ID", example = "1") @PathVariable Long id,
            @Parameter(description = "인수증 파일") @RequestPart("file") MultipartFile file,
            @Parameter(description = "수령자 이름") @RequestParam("receiverName") String receiverName,
            @Parameter(description = "수령 장소") @RequestParam("location") String location
    ) throws Exception {

        String key = "receipts/" + id + "/" + LocalDateTime.now() + "_" + file.getOriginalFilename();
        s3FileService.upload(BUCKET, key, file);

        String fileUrl = "https://" + BUCKET + ".s3.ap-northeast-2.amazonaws.com/" + key;

        service.uploadReceipt(id, fileUrl, receiverName, location);

        return ApiResponse.success("인수증이 업로드되었습니다.", null);
    }

    @Operation(summary = "리포트 업로드", description = "협업 완료 후 리포트 문서를 업로드합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)))
    @PostMapping(value = "/{id}/report", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Void> uploadReport(
            @Parameter(description = "협업 ID", example = "1") @PathVariable Long id,
            @Parameter(description = "리포트 파일") @RequestPart("file") MultipartFile file,
            @Parameter(description = "리포트 제목")
            @RequestParam(value = "title", required = false, defaultValue = "협업 진행 리포트") String title
    ) throws Exception {

        String key = "reports/" + id + "/" + LocalDateTime.now() + "_" + file.getOriginalFilename();
        s3FileService.upload(BUCKET, key, file);

        String fileUrl = "https://" + BUCKET + ".s3.ap-northeast-2.amazonaws.com/" + key;

        service.uploadReport(id, fileUrl, title);

        return ApiResponse.success("리포트가 업로드되었습니다.", null);
    }

    @Operation(summary = "설문 완료 처리", description = "기업이 요청한 설문을 완료 처리합니다.")
    @PostMapping("/{id}/survey/complete")
    public ApiResponse<Void> completeSurvey(
            @Parameter(description = "협업 ID", example = "1") @PathVariable Long id,
            @RequestBody(required = false) SurveyCompleteRequest req) {
        service.completeSurvey(id);
        return ApiResponse.success("설문이 완료되었습니다.", null);
    }
}
