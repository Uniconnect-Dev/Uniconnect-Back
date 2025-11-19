package com.uniConnect.qna.controller;

import com.uniConnect.global.response.ApiResponse;
import com.uniConnect.qna.service.QnaService;
import com.uniConnect.member.security.local.CustomUser;
import com.uniConnect.member.security.local.JwtUtil;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/qna")
@RequiredArgsConstructor
public class AdminQnaController {

    private final QnaService service;

    @PostMapping("/{id}/answer")
    public ApiResponse<String> writeAnswer(
            @PathVariable Long id,
            Authentication authentication,
            @RequestBody String content
    ) {
        CustomUser user = (CustomUser) authentication.getPrincipal();
        Long adminUserId = user.getUserId();

        service.createAnswer(id, adminUserId, content);
        return ApiResponse.success("답변이 등록되었습니다.");
    }
}