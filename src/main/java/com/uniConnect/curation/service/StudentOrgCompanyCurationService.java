package com.uniConnect.curation.service;

import com.uniConnect.curation.dto.CompanyCurationResponse;
import com.uniConnect.curation.dto.StudentOrgCompanyCurationRequest;
import com.uniConnect.global.exception.CustomException;
import com.uniConnect.global.exception.ErrorCode;
import com.uniConnect.sampling.entity.SamplingRequest;
import com.uniConnect.sampling.entity.SamplingTargetSelection;
import com.uniConnect.sampling.repository.SamplingRequestRepository;
import com.uniConnect.company.entity.Company;
import com.uniConnect.company.repository.CompanyRepository;
import com.uniConnect.studentOrg.entity.StudentOrg;
import com.uniConnect.studentOrg.entity.StudentOrgAvailability;
import com.uniConnect.studentOrg.repository.StudentOrgRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentOrgCompanyCurationService {

    private final StudentOrgRepository studentOrgRepository;
    private final CompanyRepository companyRepository;
    private final SamplingRequestRepository samplingRequestRepository;

    @PersistenceContext
    private EntityManager em;

    public List<CompanyCurationResponse> curate(
            Long studentOrgId,
            StudentOrgCompanyCurationRequest req
    ) {

        StudentOrg org = studentOrgRepository.findById(studentOrgId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROFILE_NOT_FOUND));

        List<Company> companies = companyRepository.findAllWithUsers();

        Set<String> orgTags = fetchStudentOrgHashtags(org);

        List<CompanyCurationResponse> results = new ArrayList<>();

        for (Company c : companies) {

            Long companyUserId =
                    c.getUsers().isEmpty() ? null : c.getUsers().get(0).getUserId();

            if (companyUserId == null) continue;

            // 1) 최신 샘플링 요청 ID 조회
            List<Long> reqIds =
                    samplingRequestRepository.findLatestIdByUserId(companyUserId);

            if (reqIds.isEmpty()) continue;

            Long reqId = reqIds.get(0);

            // 2) selections를 fetch join으로 조회
            SamplingRequest sReq =
                    samplingRequestRepository.findWithSelections(reqId)
                            .orElse(null);

            if (sReq == null) continue;

            // 3) 기업 태그
            Set<String> companyTags = sReq.getSelections().stream()
                    .map(SamplingTargetSelection::getSelectedLabel)
                    .map(String::toLowerCase)
                    .collect(Collectors.toSet());

            // 4) 태그 교집합
            Set<String> intersection = new HashSet<>(orgTags);
            intersection.retainAll(companyTags);
            // if (intersection.isEmpty()) continue;

            // 5) 가용성 체크
            boolean available = isAvailable(org, sReq.getEventStartDate(), sReq.getEventEndDate());

            // 6) 규모 점수
            double scaleScore = computeScaleScore(org, sReq);

            // 7) 최종 점수
            double score = intersection.size() * 0.7 + scaleScore * 0.3;
            if (!available) score *= 0.8;

            results.add(
                    CompanyCurationResponse.builder()
                            .companyId(c.getCompanyId())
                            .brandName(c.getBrandName())
                            .logoUrl(c.getLogoUrl())
                            .industry(c.getIndustry() != null ?
                                    c.getIndustry().getIndustryName() : null)
                            .tagMatchCount(intersection.size())
                            .scaleScore(scaleScore)
                            .available(available)
                            .score(score)
                            .build()
            );
        }

        results.sort(Comparator.comparingDouble(CompanyCurationResponse::getScore).reversed());
        return results;
    }

    private Set<String> fetchStudentOrgHashtags(StudentOrg org) {
        String jpql = """
            SELECT h.name
            FROM StudentOrgKeyword k
            JOIN k.hashtag h
            WHERE k.studentOrg = :org
        """;

        return new HashSet<>(
                em.createQuery(jpql, String.class)
                        .setParameter("org", org)
                        .getResultList()
        );
    }

    private boolean isAvailable(StudentOrg org, LocalDate start, LocalDate end) {
        String jpql = """
            SELECT a
            FROM StudentOrgAvailability a
            WHERE a.studentOrg = :org
            AND NOT (a.endDate < :start OR a.startDate > :end)
        """;

        return em.createQuery(jpql, StudentOrgAvailability.class)
                .setParameter("org", org)
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList()
                .isEmpty();
    }

    private double computeScaleScore(StudentOrg org, SamplingRequest req) {

        OptionalDouble exposureAvg = org.getAvailabilities().stream()
                .filter(a -> a.getExposureCount() != null)
                .mapToInt(StudentOrgAvailability::getExposureCount)
                .average();

        int exposure = (int) exposureAvg.orElse(0);
        int qty = req.getRequestedQuantity() != null ? req.getRequestedQuantity() : 1;

        if (exposure >= qty * 0.5 && exposure <= qty * 2) return 1.0;
        return 0.3;
    }
}