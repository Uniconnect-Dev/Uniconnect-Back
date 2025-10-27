package com.uniConnect.sampling.controller;

import com.uniConnect.sampling.dto.*;
import com.uniConnect.sampling.service.SamplingTargetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Sampling Target API", description = "기업 단위 대학생 타깃 키워드 선택/조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sampling")
public class SamplingTargetController {

    private final SamplingTargetService samplingTargetService;

    @Operation(summary = "타깃 키워드 저장", description = "기업(로그인 사용자)이 카테고리별 선호 키워드를 저장하고, 저장된 결과를 응답으로 반환합니다.")
    @PostMapping("/keyword")
    public ResponseEntity<SamplingTargetResponseDto> saveTargetSelection(@RequestBody SamplingTargetRequestDto dto) {
        SamplingTargetResponseDto response = samplingTargetService.saveTargetSelection(dto); // ✅ 선언 추가
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "타깃 키워드 조회", description = "기업(로그인 사용자)의 선호 키워드와 전체 선택 옵션을 조회합니다.")
    @GetMapping("/keyword")
    public ResponseEntity<SamplingTargetResponseDto> getTargetSelection() {
        return ResponseEntity.ok(samplingTargetService.getTargetSelection());
    }
}
