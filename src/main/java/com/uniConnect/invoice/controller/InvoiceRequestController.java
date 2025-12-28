package com.uniConnect.invoice.controller;

import com.uniConnect.global.response.ApiResponse;
import com.uniConnect.invoice.dto.request.InvoiceRequestCreateDto;
import com.uniConnect.invoice.service.InvoiceRequestService;
import com.uniConnect.invoice.dto.response.InvoiceMyRequestDto;
import com.uniConnect.invoice.swagger.InvoiceRequestSwaggerSchema;
import com.uniConnect.invoice.entity.InvoiceType;

import com.uniConnect.member.security.local.CustomUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;
@RestController
@RequestMapping("/api/invoice")
@RequiredArgsConstructor
public class InvoiceRequestController {

    private final InvoiceRequestService invoiceRequestService;

    @Operation(
            summary = "세금계산서 발행 요청",
            description = "학생단체가 세금계산서 발행 대행을 요청합니다.",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(implementation = InvoiceRequestSwaggerSchema.class)
                    )
            )
    )
    @PostMapping(value = "/request", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Long> requestInvoice(
            @RequestParam String eventName,
            @RequestParam Long receivedAmount,
            @RequestParam String bizNumber,
            @RequestParam String companyName,
            @RequestParam String ceoName,
            @RequestParam String address,
            @RequestParam String bizType,
            @RequestParam String contactEmail,
            @RequestParam InvoiceType invoiceType,
            @RequestParam String companyContactPhone,
            @RequestPart("bizCertFile") MultipartFile bizCertFile,
            @RequestPart(value = "contractFile", required = false) MultipartFile contractFile
    ) {

        InvoiceRequestCreateDto dto = InvoiceRequestCreateDto.builder()
                .eventName(eventName)
                .receivedAmount(receivedAmount)
                .bizNumber(bizNumber)
                .companyName(companyName)
                .ceoName(ceoName)
                .address(address)
                .bizType(bizType)
                .contactEmail(contactEmail)
                .invoiceType(invoiceType)
                .companyContactPhone(companyContactPhone)
                .bizCertFile(bizCertFile)
                .contractFile(contractFile)
                .build();

        Long id = invoiceRequestService.createInvoiceRequest(dto);

        return ApiResponse.success("세금계산서 발행 요청 완료", id);
    }

    @Operation(
            summary = "내 세금계산서 발행 요청 조회",
            description = "학생단체가 본인이 요청한 세금계산서 발행 요청 내역을 조회합니다."
    )
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<InvoiceMyRequestDto>>> getMyInvoiceRequests(
            @AuthenticationPrincipal CustomUser user) {

        List<InvoiceMyRequestDto> list = invoiceRequestService.getMyRequests(user.getUserId());

        return ResponseEntity.ok(ApiResponse.success("내 세금계산서 요청 목록 조회 성공", list));
    }
}
