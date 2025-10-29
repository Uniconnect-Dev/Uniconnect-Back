package com.uniConnect.report.repository;

import com.uniConnect.report.dto.ReportListResponseDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class SamplingReportRepositoryImpl implements SamplingReportRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<ReportListResponseDto> searchReports(
            Long studentOrgId,
            String productName,
            LocalDate dateFrom,
            LocalDate dateTo,
            int page,
            int size
    ) {
        StringBuilder sb = new StringBuilder();
        sb.append("""
            SELECT new com.uniConnect.report.dto.ReportListResponseDto(
                r.reportId,
                c.startDate,
                so.organizationName,
                c.productName,
                c.brandName,
                c.locationName,
                c.distributedQuantity,
                r.purchaseIntentPct,
                c.status,
                CASE WHEN r.reportPdfUrl IS NOT NULL THEN true ELSE false END
            )
            FROM SamplingReport r
            JOIN r.campaign c
            JOIN c.studentOrg so
            WHERE 1=1
        """);

        if (studentOrgId != null) {
            sb.append(" AND so.studentOrgId = :studentOrgId ");
        }
        if (productName != null && !productName.isBlank()) {
            sb.append(" AND c.productName LIKE :productName ");
        }
        if (dateFrom != null) {
            sb.append(" AND c.startDate >= :dateFrom ");
        }
        if (dateTo != null) {
            sb.append(" AND c.startDate < :dateToEnd ");
        }

        sb.append(" ORDER BY c.startDate DESC ");

        TypedQuery<ReportListResponseDto> query =
                em.createQuery(sb.toString(), ReportListResponseDto.class);

        if (studentOrgId != null) {
            query.setParameter("studentOrgId", studentOrgId);
        }
        if (productName != null && !productName.isBlank()) {
            query.setParameter("productName", "%" + productName + "%");
        }
        if (dateFrom != null) {
            query.setParameter("dateFrom", dateFrom.atStartOfDay());
        }
        if (dateTo != null) {
            LocalDateTime endExclusive = dateTo.plusDays(1).atStartOfDay();
            query.setParameter("dateToEnd", endExclusive);
        }

        query.setFirstResult(page * size);
        query.setMaxResults(size);

        return query.getResultList();
    }
}
