package com.uniConnect.collaboration.controller;

import com.uniConnect.collaboration.dto.*;
import com.uniConnect.collaboration.service.CollaborationDashboardService;
import com.uniConnect.member.security.local.CustomUser;
import com.uniConnect.member.entity.User;
import com.uniConnect.member.enums.UserRole;
import com.uniConnect.member.repository.UserRepository;
import com.uniConnect.member.security.local.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import com.uniConnect.member.repository.LocalCredentialRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.bind.annotation.RequestBody;

import org.springframework.web.multipart.MultipartFile;
import com.uniConnect.global.response.ApiResponse;
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
    private final LocalCredentialRepository localCredentialRepository;

    // 협업 대시보드 전체 조회
    @Operation(summary = "협업 대시보드 조회", description = "기업/학생단체 공용 협업 대시보드를 조회합니다.")
    @GetMapping("/{collaborationId}/dashboard")
    public ApiResponse<CollaborationDashboardResponse> getDashboard(
            @PathVariable Long collaborationId,
            HttpServletRequest request
    ) {
        String token = jwtUtil.resolveToken(request);

        Claims claims = jwtUtil.parseClaims(token);

        Long userId = Long.valueOf(claims.get("userId").toString());
        String role = claims.get("role").toString();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다: " + userId));

        CollaborationDashboardResponse response =
                dashboardService.getDashboard(collaborationId, userId, role);

        return ApiResponse.success("대시보드 조회 성공", response);
    }

    // 기업: 제품 정보 등록
    @Operation(
            summary = "기업 - 제품 정보 등록",
            description = "기업이 협업 중 공유할 제품명, 개수, 설명 등을 입력합니다."
    )
    @PostMapping("/company/product-info")
    public ResponseEntity<ProductInfoResponse> addProductInfo(
            @RequestBody ProductInfoRequest request,
            @AuthenticationPrincipal CustomUser user
    ) {
        Long companyUserId = user.getUserId(); // JWT에서 userId 추출

        ProductInfoResponse response = dashboardService.addOrUpdateProductInfo(
                request,
                companyUserId
        );
        return ResponseEntity.ok(response);
    }

    // 기업: 배송 정보 입력
    @Operation(
            summary = "기업 - 배송 정보 입력",
            description = "기업이 발송일자, 배송 여부, 운송장 번호를 입력합니다. "
                    + "발송 완료 시 협업 상태가 WaitingReceipt로 변경됩니다."
    )
    @PatchMapping("/company/shipping-info")
    public ResponseEntity<ProductInfoResponse> updateShippingInfo(
            @RequestBody ShippingInfoRequest request
    ) {
        return ResponseEntity.ok(dashboardService.updateShippingInfo(request));
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
        ContentUploadResponse response =
                dashboardService.uploadContentToS3(collaborationId, uploaderType, caption, image);
        return ApiResponse.success("이미지 업로드 완료", response);
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
            @Parameter(description = "협업 ID", required = true)
            @RequestPart("collaborationId") Long collaborationId,

            @Parameter(description = "수령자 이름", required = true)
            @RequestPart("receiverName") String receiverName,

            @Parameter(description = "수령 장소", required = false)
            @RequestPart(value = "location", required = false) String location,

            @Parameter(
                    description = "인수증 이미지 파일",
                    required = true,
                    content = @Content(mediaType = "multipart/form-data",
                            schema = @Schema(type = "string", format = "binary"))
            )
            @RequestPart("receiptImage") MultipartFile receiptImage
    ) throws Exception {
        ReceiptResponse response =
                dashboardService.submitReceiptToS3(collaborationId, receiverName, location, receiptImage);
        return ApiResponse.success("인수증 업로드 완료", response);
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
            @RequestBody DateFixRequest request
    ) {
        return ResponseEntity.ok(dashboardService.fixEventDate(request));
    }
}
