package com.uniConnect.collaboration.controller;

import com.uniConnect.collaboration.dto.*;
import com.uniConnect.collaboration.service.CollaborationDashboardService;
import com.uniConnect.member.security.local.CustomUser;
import com.uniConnect.global.response.ApiResponse;
import com.uniConnect.member.entity.User;
import com.uniConnect.member.enums.UserRole;
import com.uniConnect.member.repository.UserRepository;
import com.uniConnect.member.security.local.JwtUtil;
import com.uniConnect.signature.dto.SignatureRequest;
import jakarta.servlet.http.HttpServletRequest;
import com.uniConnect.member.repository.LocalCredentialRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.bind.annotation.RequestBody;

import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Tag(
        name = "협업 대시보드 API",
        description = "기업과 학생단체가 매칭 후 진행하는 실무 협업 현황 관리 기능을 제공합니다."
)
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class CollaborationDashboardController {

    private final CollaborationDashboardService dashboardService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    /* ======================
       협업 대시보드 조회
     ====================== */
    @Operation(summary = "협업 대시보드 조회")
    @GetMapping("/{collaborationId}")
    public ApiResponse<CollaborationDashboardResponse> getDashboard(
            @PathVariable Long collaborationId,
            HttpServletRequest request
    ) {
        String token = jwtUtil.resolveToken(request);
        Claims claims = jwtUtil.parseClaims(token);

        Long userId = Long.valueOf(claims.get("userId").toString());
        String role = claims.get("role").toString();

        return ApiResponse.success(
                "대시보드 조회 성공",
                dashboardService.getDashboard(collaborationId, userId, role)
        );
    }

    /* ======================
       기업 – 제품 정보 등록
     ====================== */
    @Operation(summary = "기업 - 제품 정보 등록")
    @PostMapping("/company/product-info")
    public ResponseEntity<ProductInfoResponse> addProductInfo(
            @RequestBody ProductInfoRequest request,
            @AuthenticationPrincipal CustomUser user
    ) {
        return ResponseEntity.ok(
                dashboardService.addOrUpdateProductInfo(request, user.getUserId())
        );
    }

    /* ======================
       기업 – 배송 정보 입력
     ====================== */
    @Operation(summary = "기업 - 배송 정보 입력")
    @PatchMapping("/company/shipping-info")
    public ResponseEntity<ProductInfoResponse> updateShippingInfo(
            @RequestBody ShippingInfoRequest request,
            @AuthenticationPrincipal CustomUser user
    ) {
        return ResponseEntity.ok(
                dashboardService.updateShippingInfo(request, user.getUserId())
        );
    }


    @Operation(
            summary = "이미지 업로드 (S3)",
            description = "기업 또는 학생단체가 제품 사진, 로고, 카드뉴스 등을 업로드합니다. Form-Data 형식으로 전송."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "조회 성공"
    )
    @PostMapping(value = "/content/upload", consumes = "multipart/form-data")
    public ApiResponse<ContentUploadResponse> uploadContent(
            @Parameter(description = "협업 ID", required = true)
            @RequestPart("collaborationId") Long collaborationId,

            @Parameter(description = "업로더 유형 (Company / StudentOrg / Admin)", required = true)
            @RequestPart("uploaderType") String uploaderType,

            @Parameter(description = "이미지 설명", required = false)
            @RequestPart(value = "caption", required = false) String caption,

            @Parameter(
                    description = "업로드할 이미지 파일 (JPG, PNG 등)",
                    required = true,
                    content = @Content(mediaType = "multipart/form-data",
                            schema = @Schema(type = "string", format = "binary"))
            )
            @RequestPart("image") MultipartFile image
    ) throws Exception {

        return ApiResponse.success(
                "이미지 업로드 완료",
                dashboardService.uploadContentToS3(collaborationId, uploaderType, caption, image)
        );
    }

    // 학생단체 인수증 제출
    @Operation(
            summary = "학생단체 - 인수증 제출",
            description = "학생단체가 제품 수령 후 인수증 이미지를 업로드합니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "조회 성공"
    )
    @PostMapping(value = "/receipt/submit", consumes = "multipart/form-data")
    public ApiResponse<ReceiptResponse> submitReceipt(
            @RequestPart("json") ReceiptSubmitRequest request,
            @RequestPart("receiptImage") MultipartFile receiptImage,
            @AuthenticationPrincipal CustomUser user
    ) throws Exception {
        Long userId = user.getUserId();

        return ApiResponse.success(
                "인수증 제출 완료",
                dashboardService.submitReceipt(request, receiptImage, userId)
        );
    }

    // 기업: 인수증 승인
    @Operation(
            summary = "기업 - 인수증 승인",
            description = "기업이 학생단체가 제출한 인수증을 검토 후 전자 서명(승인)을 완료합니다. "
                    + "승인 시 협업 상태가 Completed로 변경됩니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "인수증 승인 완료",
            content = @io.swagger.v3.oas.annotations.media.Content(
                    schema = @io.swagger.v3.oas.annotations.media.Schema(
                            implementation = ReceiptResponse.class
                    )
            )
    )
    public ApiResponse<ReceiptResponse> approveReceipt(
            @PathVariable Long collaborationId,
            @AuthenticationPrincipal CustomUser user
    ) {
        return ApiResponse.success(
                "인수증 승인 완료",
                dashboardService.approveReceipt(collaborationId, user.getUserId())
        );
    }

    // 기업: 행사 날짜 확정
    @Operation(
            summary = "기업 - 행사 날짜 픽스",
            description = "기업이 캘린더를 통해 행사 일자를 지정합니다. "
                    + "해당 날짜는 관련 Task의 마감일로 등록됩니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "행사 날짜 등록 완료",
            content = @io.swagger.v3.oas.annotations.media.Content(
                    schema = @io.swagger.v3.oas.annotations.media.Schema(
                            implementation = ReceiptResponse.class
                    )
            )
    )
    @PatchMapping("/company/date-fix")
    public ResponseEntity<TaskResponse> fixDate(
            @RequestBody DateFixRequest request,
            @AuthenticationPrincipal CustomUser user
    ) {
        return ResponseEntity.ok(
                dashboardService.fixEventDate(request, user.getUserId())
        );
    }
}