package com.uniConnect.survey.util;

import com.uniConnect.survey.entity.SurveyResponse;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.*;
import java.util.List;

public class CsvUtil {

    public static ByteArrayInputStream responsesToCsv(List<SurveyResponse> responses) {
        final CSVFormat format = CSVFormat.DEFAULT.withHeader(
                "Response ID", "Summary", "File URL", "Created At"
        );

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             CSVPrinter printer = new CSVPrinter(new PrintWriter(out), format)) {

            for (SurveyResponse r : responses) {
                printer.printRecord(
                        r.getResponseId(),
                        r.getSummaryText(),
                        r.getFileUrl(),
                        r.getCreatedAt()
                );
            }

            printer.flush();
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("CSV 생성 오류", e);
        }
    }
}