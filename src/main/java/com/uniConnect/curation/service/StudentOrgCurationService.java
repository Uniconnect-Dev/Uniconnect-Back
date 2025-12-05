package com.uniConnect.curation.service;

import com.uniConnect.curation.dto.StudentOrgCurationRequest;
import com.uniConnect.curation.dto.StudentOrgCurationResponse;
import com.uniConnect.studentOrg.entity.*;
import com.uniConnect.studentOrg.repository.StudentOrgRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentOrgCurationService {

    private final StudentOrgRepository studentOrgRepository;

    @PersistenceContext
    private EntityManager em;

    public List<StudentOrgCurationResponse> curate(StudentOrgCurationRequest req) {

        if (req.getQTags() == null || req.getQTags().isEmpty()) {
            throw new IllegalArgumentException("qTags must not be null or empty");
        }
        List<StudentOrg> allGroups = studentOrgRepository.findAll();

        return allGroups.stream()
                //.filter(org -> !Boolean.TRUE.equals(org.getSafetyFlag()))
                //.filter(org -> org.getVerificationLevel() != null && org.getVerificationLevel() >= 2)
                .map(org -> toResponse(org, req))
                .filter(resp -> resp.getMatchingScore() > 0)
                .sorted(Comparator.comparingDouble(StudentOrgCurationResponse::getMatchingScore).reversed())
                .collect(Collectors.toList());
    }

    private StudentOrgCurationResponse toResponse(StudentOrg org, StudentOrgCurationRequest req) {

        // 해시태그 교집합
        List<String> orgTags = Optional.ofNullable(getHashtagNames(org)).orElse(Collections.emptyList());
        Set<String> queryTags = req.getQTags().stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        Set<String> intersection = orgTags.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(queryTags::contains)
                .collect(Collectors.toSet());

        System.out.println("OrgTags: " + orgTags);
        System.out.println("QTags: " + queryTags);
        System.out.println("Intersection: " + intersection);

        boolean available = isAvailable(org, req.getSamplingStartDate(), req.getSamplingEndDate());
        double sizeScore = 1.0;
        double totalScore = intersection.size() * sizeScore;
        if (!available) totalScore *= 0.8;

        return StudentOrgCurationResponse.builder()
                .groupId(org.getStudentOrgId())
                .groupName(org.getOrganizationName())
                .hashtags(orgTags)
                .matchingScore(totalScore)
                .available(available)
                .safetyApproved(true)
                .build();
    }

    private List<String> getHashtagNames(StudentOrg org) {
        if (org == null || org.getStudentOrgId() == null) {
            System.out.println("⚠️ org 또는 orgId가 null입니다.");
            return Collections.emptyList();
        }

        String jpql = """
        SELECT h.name
        FROM StudentOrgKeyword k
        JOIN k.hashtag h
        JOIN k.studentOrg s
        WHERE s.studentOrgId = :id
    """;

        System.out.println("🔹 JPQL 실행 (id=" + org.getStudentOrgId() + ")");

        List<String> result = em.createQuery(jpql, String.class)
                .setParameter("id", org.getStudentOrgId())
                .getResultList();

        System.out.println("➡️ JPQL 결과 = " + result);
        return result;
    }


    private boolean isAvailable(StudentOrg org, LocalDate start, LocalDate end) {
        String jpql = """
            SELECT a 
            FROM StudentOrgAvailability a 
            WHERE a.studentOrg = :org
            AND NOT (a.endDate < :start OR a.startDate > :end)
        """;
        List<StudentOrgAvailability> conflicts = em.createQuery(jpql, StudentOrgAvailability.class)
                .setParameter("org", org)
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList();
        return conflicts.isEmpty();
    }

    private StudentOrgCurationResponse buildEmpty(StudentOrg org) {
        return StudentOrgCurationResponse.builder()
                .groupId(org.getStudentOrgId())
                .groupName(org.getOrganizationName())
                .hashtags(Collections.emptyList())
                .matchingScore(0)
                .available(true)
                .safetyApproved(false)
                .build();
    }
}
