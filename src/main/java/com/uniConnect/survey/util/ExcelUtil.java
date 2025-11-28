package com.uniConnect.survey.util;

import com.uniConnect.survey.entity.SurveyResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.List;

public class ExcelUtil {

    public static ByteArrayInputStream responsesToExcel(List<SurveyResponse> responses) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Responses");

            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Response ID");
            headerRow.createCell(1).setCellValue("Summary");
            headerRow.createCell(2).setCellValue("File URL");
            headerRow.createCell(3).setCellValue("Created At");

            int rowIdx = 1;
            for (SurveyResponse r : responses) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(r.getResponseId());
                row.createCell(1).setCellValue(r.getSummaryText());
                row.createCell(2).setCellValue(r.getFileUrl());
                row.createCell(3).setCellValue(r.getCreatedAt().toString());
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());

        } catch (IOException e) {
            throw new RuntimeException("엑셀 생성 오류", e);
        }
    }
}