package com.uniConnect.contract.controller;

import com.uniConnect.contract.dto.ContractResponseDto;
import com.uniConnect.contract.dto.ContractSignRequestDto;
import com.uniConnect.contract.dto.ContractSignResponseDto;
import com.uniConnect.contract.service.ContractService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
@Tag(name = "Contract API", description = "계약서 조회 및 서명 관련 API")
public class ContractController {

    private final ContractService contractService;

    /**
     * 계약서 상세 조회 API
     */
    @Operation(summary = "계약서 상세 조회", description = "특정 계약서 ID로 계약서 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = ContractResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "계약서를 찾을 수 없습니다.", content = @Content)
    })
    @GetMapping("/{contractId}")
    public ResponseEntity<ContractResponseDto> getContract(@PathVariable Long contractId) {
        ContractResponseDto response = contractService.getContract(contractId);
        return ResponseEntity.ok(response);
    }

    /**
     * 학생 서명 전송 API
     */
    @Operation(summary = "학생 서명 전송", description = "학생이 전자 서명을 업로드하여 계약을 완료합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "서명 성공",
                    content = @Content(schema = @Schema(implementation = ContractSignResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "이미 서명된 계약입니다.", content = @Content),
            @ApiResponse(responseCode = "404", description = "계약서를 찾을 수 없습니다.", content = @Content)
    })
    @PostMapping("/{contractId}/sign")
    public ResponseEntity<ContractSignResponseDto> signContract(
            @PathVariable Long contractId,
            @RequestBody ContractSignRequestDto requestDto
    ) {
        ContractSignResponseDto response = contractService.signContract(contractId, requestDto);
        return ResponseEntity.ok(response);
    }
}
