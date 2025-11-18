package com.uniConnect.compliance.controller;

import com.uniConnect.compliance.dto.ComplianceAgreementRequest;
import com.uniConnect.compliance.dto.ComplianceAgreementResponse;
import com.uniConnect.compliance.service.ComplianceService;
import com.uniConnect.global.response.ApiResponse;
import com.uniConnect.member.security.local.CustomUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/compliance")
@RequiredArgsConstructor
@Tag(name = "Compliance", description = "샘플링 규정 동의 API")
public class ComplianceController {

    private final ComplianceService complianceService;

    @PostMapping
    @Operation(summary = "샘플링 규정 동의", description = """
            **샘플링 진행을 위한 필수 규정 확인 및 동의 처리**
            
            ### 필수 동의 항목 (3가지 모두 필수)
            1. **PROCESS_INFO**: 이후 프로세스 안내
               - 학생 단체가 승인하면 매칭 확정
               - 계약서와 제품 설문지 활성화
               - 거절 시 샘플링 불가
            
            2. **OFFPLATFORM_PENALTY**: 플랫폼 이탈 협업 적발 시 불이익 안내
               - 플랫폼 외부 직거래/협업 금지
               - 적발 시 서비스 이용 제한 및 캠페인 참여 불가
            
            3. **TERMS_ACK**: 이용 약관 확인 안내
               - 사용자의 인지 및 이해는 사용자 책임
               - UNI:CONNECT는 이로 인한 문제에 책임지지 않음
            
            ### 주의사항
            - **모든 항목 필수**: 3가지 모두 포함되어야 함
            - **모두 true 필수**: 각 항목의 accepted가 모두 true여야 함
            - **중복 불가**: requestId당 1회만 동의 가능
            - **JWT 인증 필수**: Bearer 토큰 필요
            
            ### 프론트엔드 플로우
            1. 사용자에게 3개 팝업을 순차적으로 표시
            2. 각 팝업에서 "확인했습니다" 체크박스 선택
            3. 모두 체크 후 이 API 호출
            """,
            tags = {"Compliance"})
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "동의 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "성공 응답",
                                    value = """
                        {
                          "success": true,
                          "message": "요청이 성공적으로 처리되었습니다.",
                          "data": {
                            "requestId": "mr_9001",
                            "agreementId": 1,
                            "status": "COMPLETED",
                            "agreementDetails": [
                              {
                                "type": "PROCESS_INFO",
                                "accepted": true,
                                "description": "학생 단체가 승인할 경우 매칭이 확정되며..."
                              },
                              {
                                "type": "OFFPLATFORM_PENALTY",
                                "accepted": true,
                                "description": "UNI:CONNECT는 기업과 학생단체 간의..."
                              },
                              {
                                "type": "TERMS_ACK",
                                "accepted": true,
                                "description": "이용약관에 대한 사용자의 인지 및..."
                              }
                            ],
                            "agreedAt": "2025-11-06T10:30:00",
                            "message": "샘플링 규정 동의가 완료되었습니다."
                          }
                        }
                        """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (항목 누락 또는 동의하지 않음)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "항목 누락",
                                            value = """
                            {
                              "success": false,
                              "message": "모든 항목에 동의해야 합니다.",
                              "data": null
                            }
                            """
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "이미 동의한 요청",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                        {
                          "success": false,
                          "message": "이미 프로필이 존재합니다.",
                          "data": null
                        }
                        """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 요청 (JWT 토큰 없음 또는 만료)"
            )})
    public ResponseEntity<ApiResponse<ComplianceAgreementResponse>> agreeToCompliance(
            @AuthenticationPrincipal CustomUser customUser,
            @Valid @RequestBody ComplianceAgreementRequest request,
            HttpServletRequest httpRequest
    ) {
        ComplianceAgreementResponse response = complianceService.agreeToCompliance(
                customUser.getUserId(),
                request,
                httpRequest
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{requestId}")
    @Operation(summary = "동의 내역 조회", description = "특정 requestId의 동의 내역 조회")
    public ResponseEntity<ApiResponse<ComplianceAgreementResponse>> getAgreement(
            @PathVariable String requestId
    ) {
        ComplianceAgreementResponse response = complianceService.getAgreementByRequestId(requestId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}