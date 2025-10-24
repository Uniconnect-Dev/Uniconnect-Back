package com.uniConnect.contract.controller;

import com.uniConnect.contract.dto.ContractListItemDto;
import com.uniConnect.contract.dto.ContractResponseDto;
import com.uniConnect.contract.dto.ContractSignRequestDto;
import com.uniConnect.contract.dto.ContractSignResponseDto;
import com.uniConnect.contract.dto.CompanySignRequestDto;
import com.uniConnect.contract.dto.CompanySignResponseDto;
import com.uniConnect.contract.dto.ContractPdfResponseDto;
import com.uniConnect.contract.dto.ReceiptPreviewResponseDto;
import com.uniConnect.contract.dto.ReceiptSignRequestDto;
import com.uniConnect.contract.dto.ReceiptSignResponseDto;
import com.uniConnect.contract.service.ContractService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
     * 계약 리스트 조회 API
     */
    @Operation(
            summary = "계약서 목록 조회",
            description = "학생 단체 또는 관리자 기준으로 계약서 목록을 조회합니다. (매칭 정보, 상태, PDF URL 포함)"
    )
    @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(schema = @Schema(implementation = ContractListItemDto.class)))
    @GetMapping("")
    public ResponseEntity<List<ContractListItemDto>> getContracts() {
        List<ContractListItemDto> list = contractService.getMyContracts();
        return ResponseEntity.ok(list);
    }

    /**
     * 학생 서명 전송 API
     */
    @Operation(
            summary = "학생 전자서명 전송",
            description = "학생이 계약서에 전자서명을 업로드하여 서명을 완료합니다. 상태가 PENDING_SIGNATURE → STUDENT_SIGNED로 변경됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "서명 성공",
                    content = @Content(schema = @Schema(implementation = ContractSignResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "이미 서명된 계약입니다.", content = @Content),
            @ApiResponse(responseCode = "404", description = "계약서를 찾을 수 없습니다.", content = @Content)
    })
    @PostMapping("/{contractId}/sign")
    public ResponseEntity<ContractSignResponseDto> signContract(
            @Parameter(description = "서명할 계약서 ID", example = "1")
            @PathVariable Long contractId,
            @RequestBody ContractSignRequestDto requestDto
    ) {
        ContractSignResponseDto response = contractService.signContract(contractId, requestDto);
        return ResponseEntity.ok(response);
    }

    /**
     * 회사(운영자) 서명 API
     */
    @Operation(
            summary = "회사 전자서명 전송",
            description = "관리자(기업 담당자)가 계약서에 전자서명을 업로드합니다. 상태가 STUDENT_SIGNED → SIGNED로 변경됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "서명 성공",
                    content = @Content(schema = @Schema(implementation = CompanySignResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "계약서를 찾을 수 없습니다.", content = @Content)
    })
    @PostMapping("/{contractId}/company-sign")
    public ResponseEntity<CompanySignResponseDto> companySignContract(
            @Parameter(description = "서명할 계약서 ID", example = "1")
            @PathVariable Long contractId,
            @RequestBody CompanySignRequestDto requestDto
    ) {
        CompanySignResponseDto dto = contractService.companySignContract(contractId, requestDto);
        return ResponseEntity.ok(dto);
    }


    /**
     * 계약서 PDF 미리보기 API
     */
    @Operation(
            summary = "계약서 PDF 미리보기",
            description = "계약서 PDF 파일의 S3 URL을 반환합니다. 프론트는 이 URL로 PDF를 렌더링할 수 있습니다."
    )
    @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(schema = @Schema(implementation = ContractPdfResponseDto.class)))
    @GetMapping("/{contractId}/pdf")
    public ResponseEntity<ContractPdfResponseDto> getContractPdf(
            @Parameter(description = "계약서 ID", example = "1")
            @PathVariable Long contractId
    ) {
        ContractPdfResponseDto dto = contractService.getContractPdf(contractId);
        return ResponseEntity.ok(dto);
    }

    /**
     * 인수증(Receipt) 미리보기 API
     */
    @Operation(
            summary = "인수증 PDF 미리보기",
            description = "계약 완료 후 발행된 인수증(Receipt) PDF의 S3 URL을 반환합니다."
    )
    @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(schema = @Schema(implementation = ReceiptPreviewResponseDto.class)))
    @GetMapping("/{contractId}/receipt")
    public ResponseEntity<ReceiptPreviewResponseDto> getReceipt(
            @Parameter(description = "계약서 ID", example = "1")
            @PathVariable Long contractId
    ) {
        ReceiptPreviewResponseDto dto = contractService.getReceipt(contractId);
        return ResponseEntity.ok(dto);
    }


    /**
     * 인수증 서명 제출 API
     */
    @Operation(
            summary = "인수증 서명 제출",
            description = "학생 단체가 인수증에 전자서명을 업로드하여 수령 완료를 표시합니다. 상태가 RECEIPT_SIGNED로 변경됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "서명 성공",
                    content = @Content(schema = @Schema(implementation = ReceiptSignResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "계약서를 찾을 수 없습니다.", content = @Content)
    })
    @PostMapping("/{contractId}/receipt/sign")
    public ResponseEntity<ReceiptSignResponseDto> signReceipt(
            @Parameter(description = "인수증 서명할 계약서 ID", example = "1")
            @PathVariable Long contractId,
            @RequestBody ReceiptSignRequestDto requestDto
    ) {
        ReceiptSignResponseDto dto = contractService.signReceipt(contractId, requestDto);
        return ResponseEntity.ok(dto);
    }

    /**
     * (관리자용) 계약 상태 강제 변경 API
     */
    @Operation(
            summary = "계약 상태 변경 (관리자용)",
            description = "관리자가 계약 상태를 직접 변경합니다. 가능한 값: PENDING_SIGNATURE, STUDENT_SIGNED, SIGNED, RECEIPT_PENDING, RECEIPT_SIGNED"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상태 변경 성공",
                    content = @Content(schema = @Schema(implementation = ContractResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 상태 값", content = @Content),
            @ApiResponse(responseCode = "404", description = "계약서를 찾을 수 없습니다.", content = @Content)
    })
    @PatchMapping("/{contractId}/status")
    public ResponseEntity<ContractResponseDto> updateStatus(
            @Parameter(description = "상태를 변경할 계약서 ID", example = "1")
            @PathVariable Long contractId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "변경할 상태 값 (예: { \"status\": \"SIGNED\" })",
                    required = true,
                    content = @Content(schema = @Schema(example = "{\"status\": \"SIGNED\"}"))
            )
            @RequestBody Map<String, String> body
    ) {
        String newStatus = body.get("status");
        ContractResponseDto dto = contractService.updateStatus(contractId, newStatus);
        return ResponseEntity.ok(dto);
    }
}
