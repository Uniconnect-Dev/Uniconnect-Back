package com.uniConnect.report.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReportPdfMetaDto {
    private Long reportId;
    private String pdfUrl;
    private String fileName;
}
