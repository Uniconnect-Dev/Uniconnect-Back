package com.uniConnect.invoice.controller;

import com.uniConnect.global.response.ApiResponse;
import com.uniConnect.invoice.dto.request.InvoiceRequestCreateDto;
import com.uniConnect.invoice.service.InvoiceRequestService;
import com.uniConnect.invoice.swagger.InvoiceRequestSwaggerSchema;
import com.uniConnect.invoice.entity.InvoiceType;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
}
