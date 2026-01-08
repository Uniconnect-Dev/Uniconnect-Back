package com.uniConnect.payment.swagger;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter @Setter
public class InvoiceRequestSwaggerSchema {

    @Schema(type = "string", format = "binary", description = "사업자등록증 파일(.jpg, .png, .pdf)")
    private MultipartFile bizCertFile;

    @Schema(type = "string", format = "binary", description = "기업 계약서 파일(.jpg, .png, .pdf, optional)")
    private MultipartFile contractFile;
}