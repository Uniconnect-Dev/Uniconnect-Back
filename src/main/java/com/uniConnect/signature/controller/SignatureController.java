package com.uniConnect.signature.controller;

import com.uniConnect.global.response.ApiResponse;
import com.uniConnect.signature.dto.SignatureRequest;
import com.uniConnect.signature.service.SignatureService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import lombok.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/signature")
@Tag(name = "Signature", description = "전자서명 API")
public class SignatureController {

    private final SignatureService signatureService;

    @PostMapping
    @Operation(summary = "전자서명 저장")
    public ApiResponse<?> saveSignature(
            @Valid @RequestBody SignatureRequest request,
            Authentication auth
    ) {

        com.uniConnect.member.security.local.CustomUser user =
                (com.uniConnect.member.security.local.CustomUser) auth.getPrincipal();

        Long userId = user.getUserId();

        signatureService.saveSignature(request, userId);
        return ApiResponse.success("전자서명 저장 완료");
    }
}

