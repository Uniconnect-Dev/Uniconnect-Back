package com.uniConnect.qna.controller;

import com.uniConnect.global.response.ApiResponse;
import com.uniConnect.qna.dto.*;
import com.uniConnect.qna.enums.*;
import com.uniConnect.qna.service.QnaService;

import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;
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
    // 기업&학생단체 공용 문의 생성
    // -----------------------------
    @PostMapping(consumes = "multipart/form-data")
    @Operation(summary = "Q&A 문의 등록 (기업/학생단체 공용)")
    public ApiResponse<Long> createQna(
            @AuthenticationPrincipal CustomUser user,

            @RequestParam String title,
            @RequestParam String content,

            @RequestPart(required = false)
            List<MultipartFile> files,

            @RequestParam Boolean agreePersonalInfo,
            @RequestParam Boolean agreeNotification
    ) {
        return ApiResponse.success(
                service.createQna(
                        user.getUserId(),
                        title,
                        content,
                        files,
                        agreePersonalInfo,
                        agreeNotification
                )
        );
    }

    // -----------------------------
    // 상세 조회
    // -----------------------------
    @GetMapping("/{id}")
    @Operation(summary = "문의 상세 조회")
    public ApiResponse<QuestionDetailResponse> get(@PathVariable Long id) {
        return ApiResponse.success(service.getQuestion(id));
    }

    @GetMapping("/my")
    @Operation(summary = "내 문의 목록 + 상태별 개수 조회")
    public ApiResponse<MyQnaSummaryResponse> myQna(
            @AuthenticationPrincipal CustomUser user
    ) {
        return ApiResponse.success(
                service.getMyQnaSummary(user.getUserId())
        );
    }

    @GetMapping("/my/pending")
    @Operation(summary = "내 답변 전 문의 목록 조회")
    public ApiResponse<List<QuestionDetailResponse>> myPendingQna(
            @AuthenticationPrincipal CustomUser user
    ) {
        return ApiResponse.success(
                service.getMyQnaByStatus(user.getUserId(), QuestionStatus.Pending)
        );
    }

    @GetMapping("/my/answered")
    @Operation(summary = "내 답변 완료 문의 목록 조회")
    public ApiResponse<List<QuestionDetailResponse>> myAnsweredQna(
            @AuthenticationPrincipal CustomUser user
    ) {
        return ApiResponse.success(
                service.getMyQnaByStatus(user.getUserId(), QuestionStatus.AnswerCompleted)
        );
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
