package com.uniConnect.collaboration.controller;

import com.uniConnect.collaboration.dto.*;
import com.uniConnect.collaboration.service.CollaborationDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartException;

@Tag(
        name = "협업 대시보드 API",
        description = "기업과 학생단체가 매칭 후 진행하는 실무 협업 현황 관리 기능을 제공합니다."
)
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class CollaborationDashboardController {

    private final CollaborationDashboardService dashboardService;

    // 대시보드 전체 조회
    @Operation(
            summary = "협업 대시보드 조회",
            description = "특정 collaborationId에 해당하는 협업 진행 현황을 조회합니다. "
                    + "기업과 학생단체 모두 접근 가능합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = CollaborationDashboardResponse.class))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 협업 ID",
                    content = @Content(schema = @Schema(example = "{\"message\":\"Collaboration not found\"}")))
    })
    @GetMapping("/{collaborationId}")
    public ResponseEntity<CollaborationDashboardResponse> getDashboard(
            @PathVariable Long collaborationId
    ) {
        return ResponseEntity.ok(
                dashboardService.getDashboard(collaborationId)
        );
    }

    // 기업: 제품 정보 등록
    @Operation(
            summary = "기업 - 제품 정보 등록",
            description = "기업이 협업 중 공유할 제품명, 개수, 설명 등을 입력합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "제품 정보 등록 성공",
                    content = @Content(schema = @Schema(implementation = ProductInfoResponse.class)))
    })
    @PostMapping("/company/product-info")
    public ResponseEntity<ProductInfoResponse> addProductInfo(
            @RequestBody ProductInfoRequest request
    ) {
        ProductInfoResponse response = dashboardService.addOrUpdateProductInfo(request);
        return ResponseEntity.ok(response);
    }

    // 기업: 배송 정보 입력
    @Operation(
            summary = "기업 - 배송 정보 입력",
            description = "기업이 발송일자, 배송 여부, 운송장 번호를 입력합니다. "
                    + "발송 완료 시 협업 상태가 WaitingReceipt로 변경됩니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "배송 정보 업데이트 성공",
            content = @Content(schema = @Schema(implementation = ProductInfoResponse.class))
    )
    @PatchMapping("/company/shipping-info")
    public ResponseEntity<ProductInfoResponse> updateShippingInfo(
            @RequestBody ShippingInfoRequest request
    ) {
        return ResponseEntity.ok(dashboardService.updateShippingInfo(request));
    }

    // 공통: 이미지 업로드 (기업/학생단체)
    @Operation(
            summary = "이미지 업로드 (기업 / 학생단체)",
            description = "기업 또는 학생단체가 제품 사진, 로고, 카드뉴스 등 이미지를 업로드합니다. "
                    + "Form-Data로 전송해야 합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "이미지 업로드 성공",
            content = @Content(schema = @Schema(implementation = ContentUploadResponse.class))
    )
    @PostMapping("/content/upload")
    public ResponseEntity<ContentUploadResponse> uploadContent(
            @ModelAttribute ContentUploadRequest request
    ) throws MultipartException {
        return ResponseEntity.ok(dashboardService.uploadContent(request));
    }

    // 학생단체: 인수증 제출
    @Operation(
            summary = "학생단체 - 인수증 제출",
            description = "학생단체가 제품 수령 후 인수증을 업로드합니다. "
                    + "인수증은 이미지 파일 형태로 Form-Data로 업로드됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "인수증 제출 성공",
                    content = @Content(schema = @Schema(implementation = ReceiptResponse.class))),
            @ApiResponse(responseCode = "400", description = "파일 누락 또는 잘못된 요청",
                    content = @Content(schema = @Schema(example = "{\"message\":\"Missing receipt image\"}")))
    })
    @PostMapping("/receipt/submit")
    public ResponseEntity<ReceiptResponse> submitReceipt(
            @ModelAttribute ReceiptUploadRequest request
    ) {
        return ResponseEntity.ok(dashboardService.submitReceipt(request));
    }

    // 기업: 인수증 승인
    @Operation(
            summary = "기업 - 인수증 승인",
            description = "기업이 학생단체가 제출한 인수증을 검토 후 전자 서명(승인)을 완료합니다. "
                    + "승인 시 협업 상태가 Completed로 변경됩니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "인수증 승인 완료",
            content = @Content(schema = @Schema(implementation = ReceiptResponse.class))
    )
    @PatchMapping("/receipt/approve")
    public ResponseEntity<ReceiptResponse> approveReceipt(
            @RequestBody ReceiptApproveRequest request
    ) {
        return ResponseEntity.ok(dashboardService.approveReceipt(request));
    }

    // 기업: 행사 날짜 확정
    @Operation(
            summary = "기업 - 행사 날짜 픽스",
            description = "기업이 캘린더를 통해 행사 일자를 지정합니다. "
                    + "해당 날짜는 관련 Task의 마감일로 등록됩니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "행사 날짜 등록 완료",
            content = @Content(schema = @Schema(implementation = TaskResponse.class))
    )
    @PatchMapping("/company/date-fix")
    public ResponseEntity<TaskResponse> fixDate(
            @RequestBody DateFixRequest request
    ) {
        return ResponseEntity.ok(dashboardService.fixEventDate(request));
    }
}
