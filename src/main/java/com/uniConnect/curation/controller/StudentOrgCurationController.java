package com.uniConnect.curation.controller;

import com.uniConnect.curation.dto.StudentOrgCurationRequest;
import com.uniConnect.curation.dto.StudentOrgCurationResponse;
import com.uniConnect.curation.service.StudentOrgCurationService;
import com.uniConnect.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@Tag(name = "StudentOrg Curation API", description = "학생단체 큐레이션 및 추천 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/curation")
public class StudentOrgCurationController {

    private final StudentOrgCurationService curationService;

    @Operation(summary = "학생단체 큐레이션 추천", description = "기업의 요청 조건을 기반으로 적합한 학생단체 리스트를 반환합니다.")
    @PostMapping("/student-orgs")
    public ResponseEntity<ApiResponse<List<StudentOrgCurationResponse>>> curate(@RequestBody StudentOrgCurationRequest request) {
        List<StudentOrgCurationResponse> result = curationService.curate(request);
        return ResponseEntity.ok(ApiResponse.success("학생단체 큐레이션 추천 결과입니다.", result));
    }
}
