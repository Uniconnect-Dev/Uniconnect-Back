package com.uniConnect.qna.controller;

import com.uniConnect.global.response.ApiResponse;
import com.uniConnect.qna.dto.QnaCreateRequest;
import com.uniConnect.qna.dto.QuestionDetailResponse;
import com.uniConnect.qna.dto.VerifyPasswordRequest;
import com.uniConnect.qna.service.QnaService;

import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.uniConnect.member.security.local.CustomUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/qna")
@RequiredArgsConstructor
@Tag(name = "Q&A API", description = "학생단체 & 기업 공용 Q&A 문의 API")
public class QnaController {

    private final QnaService service;

    // -----------------------------
    // 기업 문의 생성
    // -----------------------------
    @Operation(summary = "기업 문의 작성", description = "로그인한 기업 사용자로 Q&A 문의를 등록합니다.")
    @PostMapping("/company")
    public ApiResponse<Long> createCompanyQna(
            @AuthenticationPrincipal CustomUser user,
            @RequestBody QnaCreateRequest req
    ) {
        return ApiResponse.success(service.createCompanyQna(req, user.getUserId()));
    }

    // -----------------------------
    // 학생단체 문의 생성
    // -----------------------------
    @Operation(summary = "학생단체 문의 작성", description = "로그인한 학생단체 사용자로 Q&A 문의를 등록합니다.")
    @PostMapping("/student-org")
    public ApiResponse<Long> createStudentOrgQna(
            @AuthenticationPrincipal CustomUser user,
            @RequestBody QnaCreateRequest req
    ) {
        return ApiResponse.success(service.createStudentOrgQna(req, user.getUserId()));
    }

    // -----------------------------
    // 상세 조회
    // -----------------------------
    @Operation(summary = "문의 상세 조회", description = "특정 문의 내용과 답변 내용을 조회합니다.")
    @GetMapping("/{id}")
    public ApiResponse<QuestionDetailResponse> get(@PathVariable Long id) {
        return ApiResponse.success(service.getQuestion(id));
    }

    // -----------------------------
    // 비밀번호 검증
    // -----------------------------
    @Operation(summary = "비밀번호 검증", description = "비밀번호가 맞으면 상세 조회 가능.")
    @PostMapping("/{id}/verify-password")
    public ApiResponse<Boolean> verifyPassword(
            @PathVariable Long id,
            @RequestBody VerifyPasswordRequest req
    ) {
        return ApiResponse.success(service.verifyPassword(id, req.password()));
    }

    // -----------------------------
    // 기업 문의 리스트 조회 (JWT 기반)
    // -----------------------------
    @Operation(summary = "기업 문의 목록 조회", description = "로그인한 기업 계정의 문의 목록을 조회합니다.")
    @GetMapping("/company/list")
    public ApiResponse<List<QuestionDetailResponse>> getCompanyQnaList(
            @AuthenticationPrincipal CustomUser user
    ) {
        return ApiResponse.success(service.getCompanyQnaListByUser(user.getUserId()));
    }

    // -----------------------------
    // 학생단체 문의 리스트 조회 (JWT 기반)
    // -----------------------------
    @Operation(summary = "학생단체 문의 목록 조회", description = "로그인한 학생단체 계정의 문의 목록을 조회합니다.")
    @GetMapping("/student-org/list")
    public ApiResponse<List<QuestionDetailResponse>> getStudentOrgQnaList(
            @AuthenticationPrincipal CustomUser user
    ) {
        return ApiResponse.success(service.getStudentOrgQnaListByUser(user.getUserId()));
    }

    // -----------------------------
    // 관리자 답변 작성
    // -----------------------------
    @Operation(summary = "문의 답변 작성 (Admin)", description = "관리자가 특정 문의에 답변을 등록합니다.")
    @PostMapping("/{id}/answer")
    public ApiResponse<String> writeAnswer(
            @PathVariable Long id,
            Authentication authentication,
            @RequestBody String content
    ) {
        CustomUser user = (CustomUser) authentication.getPrincipal();
        service.createAnswer(id, user.getUserId(), content);
        return ApiResponse.success("답변이 등록되었습니다.");
    }

}
