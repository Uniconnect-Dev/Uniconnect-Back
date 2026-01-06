package com.uniConnect.matching.controller;

import com.uniConnect.global.response.ApiResponse;
import com.uniConnect.matching.service.CollaborationMatchRequestService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/match-requests")
@Tag(name = "Match Request Admin", description = "매칭 요청 승인 / 반려 API")
public class CollaborationMatchRequestController {

    private final CollaborationMatchRequestService matchRequestService;

    @PostMapping("/{id}/approve")
    @Operation(summary = "매칭 요청 승인")
    public ApiResponse<Void> approve(@PathVariable Long id) {
        matchRequestService.approve(id);
        return ApiResponse.success("매칭 승인 완료", null);
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "매칭 요청 반려")
    public ApiResponse<Void> reject(@PathVariable Long id) {
        matchRequestService.reject(id);
        return ApiResponse.success("매칭 반려 완료", null);
    }
}
