package com.uniConnect.qna.controller;

import com.uniConnect.global.response.ApiResponse;
import com.uniConnect.qna.dto.QnaCreateRequest;
import com.uniConnect.qna.dto.QuestionDetailResponse;
import com.uniConnect.qna.service.QnaService;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/qna")
@RequiredArgsConstructor
public class QnaController {

    private final QnaService service;

    @PostMapping("/company")
    public ApiResponse<Long> createCompanyQna(@RequestBody QnaCreateRequest req) {
        return ApiResponse.success(service.createQna(req));
    }

    @PostMapping("/student-org")
    public ApiResponse<Long> createStudentOrgQna(@RequestBody QnaCreateRequest req) {
        return ApiResponse.success(service.createQna(req));
    }

    @GetMapping("/{id}")
    public ApiResponse<QuestionDetailResponse> get(@PathVariable Long id) {
        return ApiResponse.success(service.getQuestion(id));
    }
}