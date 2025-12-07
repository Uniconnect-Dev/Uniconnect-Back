package com.uniConnect.invoice.service;

import com.uniConnect.invoice.dto.request.InvoiceRequestCreateDto;
import com.uniConnect.invoice.entity.InvoiceRequest;
import com.uniConnect.invoice.entity.InvoiceStatus;
import com.uniConnect.invoice.entity.InvoiceType;
import com.uniConnect.invoice.repository.InvoiceRequestRepository;
import com.uniConnect.global.exception.CustomException;
import com.uniConnect.global.exception.ErrorCode;
import com.uniConnect.s3.S3FileService;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class InvoiceRequestService {

    private final InvoiceRequestRepository invoiceRequestRepository;
    private final S3FileService s3FileService;

    @Value("${app.s3.bucket}")
    private String bucket;

    private String uploadFile(MultipartFile file, String dirName) {
        try {
            String key = dirName + "/" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
            return s3FileService.upload(bucket, key, file);
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private InvoiceType convertType(String rawType) {

        if (rawType == null) {
            throw new CustomException(ErrorCode.INVALID_INVOICE_TYPE);
        }

        String normalized = rawType
                .trim()
                .replace(" ", "_")
                .replace("-", "_")
                .toUpperCase();

        try {
            return InvoiceType.valueOf(normalized);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INVALID_INVOICE_TYPE);
        }
    }


    @Transactional
    public Long createInvoiceRequest(InvoiceRequestCreateDto dto) {

        if (dto.getBizCertFile() == null || dto.getBizCertFile().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        InvoiceType type = dto.getInvoiceType();

        String bizCertUrl = uploadFile(dto.getBizCertFile(), "invoice/biz-cert");

        String contractUrl = null;
        if (dto.getContractFile() != null && !dto.getContractFile().isEmpty()) {
            contractUrl = uploadFile(dto.getContractFile(), "invoice/contract");
        }

        InvoiceRequest saved = invoiceRequestRepository.save(
                InvoiceRequest.builder()
                        .eventName(dto.getEventName())
                        .receivedAmount(dto.getReceivedAmount())
                        .bizCertUrl(bizCertUrl)
                        .contractFileUrl(contractUrl)
                        .bizNumber(dto.getBizNumber())
                        .companyName(dto.getCompanyName())
                        .ceoName(dto.getCeoName())
                        .address(dto.getAddress())
                        .bizType(dto.getBizType())
                        .contactEmail(dto.getContactEmail())
                        .invoiceType(type)
                        .companyContactPhone(dto.getCompanyContactPhone())
                        .status(InvoiceStatus.Requested)
                        .build()
        );

        return saved.getInvoiceRequestId();
    }
}