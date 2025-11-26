package com.uniConnect.signature.controller;

import com.uniConnect.global.response.ApiResponse;
import com.uniConnect.member.security.local.CustomUser;
import com.uniConnect.signature.dto.SignatureRequest;
import com.uniConnect.signature.service.SignatureService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/signature")
@Tag(name = "Signature", description = "전자서명 API")
public class SignatureController {

    private final SignatureService signatureService;

    @PostMapping
    @Operation(
            summary = "전자서명 저장",
            description = "프론트에서 전달된 서명 데이터(signatureImage, timestamp, documentHash, signatureHash)를 검증 후 저장합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "서명 저장 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ApiResponse<?> saveSignature(
            @Valid @RequestBody SignatureRequest request,
            Authentication authentication
    ) {
        CustomUser user = (CustomUser) authentication.getPrincipal();
        signatureService.saveSignature(request, user.getUserId());
        return ApiResponse.success("전자서명 저장 완료");
    }
}
