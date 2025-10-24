package com.uniConnect.contract.controller;

import com.uniConnect.contract.dto.ContractResponseDto;
import com.uniConnect.contract.dto.ContractSignRequestDto;
import com.uniConnect.contract.dto.ContractSignResponseDto;
import com.uniConnect.contract.entity.Contract;
import com.uniConnect.contract.service.ContractService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;

    // 계약서 상세 조회
    @GetMapping("/{contractId}")
    public ResponseEntity<ContractResponseDto> getContract(@PathVariable Long contractId) {
        ContractResponseDto response = contractService.getContract(contractId);
        return ResponseEntity.ok(response);
    }

    // 학생 서명 전송
    @PostMapping("/{contractId}/sign")
    public ResponseEntity<ContractSignResponseDto> signContract(
            @PathVariable Long contractId,
            @RequestBody ContractSignRequestDto requestDto
    ) {
        ContractSignResponseDto response = contractService.signContract(contractId, requestDto);
        return ResponseEntity.ok(response);
    }
}
