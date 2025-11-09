package com.uniConnect.studentOrg.service;

import com.uniConnect.studentOrg.dto.StudentOrgCalendarResponse;
import com.uniConnect.studentOrg.dto.StudentOrgProfileDetailResponse;
import com.uniConnect.studentOrg.dto.StudentOrgProfileListResponse;
import com.uniConnect.studentOrg.entity.StudentOrg;
import com.uniConnect.studentOrg.entity.StudentOrgAvailability;
import com.uniConnect.studentOrg.entity.StudentOrgContact;
import com.uniConnect.studentOrg.entity.StudentOrgHistory;
import com.uniConnect.studentOrg.enums.CollaborationType;
import com.uniConnect.studentOrg.enums.OrganizationType;
import com.uniConnect.studentOrg.repository.StudentOrgAvailabilityRepository;
import com.uniConnect.studentOrg.repository.StudentOrgHistoryRepository;
import com.uniConnect.studentOrg.repository.StudentOrgRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentOrgProfileService {

    private final StudentOrgRepository studentOrgRepository;
    private final StudentOrgAvailabilityRepository availabilityRepository;
    private final StudentOrgHistoryRepository historyRepository;

    /** 학생단체 리스트 조회 */
    public List<StudentOrgProfileListResponse> getProfileList(
            String keyword,
            CollaborationType collaborationType,
            OrganizationType organizationType
    ) {
        List<StudentOrg> orgs = studentOrgRepository.searchProfiles(
                keyword,
                collaborationType,
                organizationType
        );

        List<StudentOrg> filtered = orgs.stream()
                .filter(o -> o.getHistories() != null && !o.getHistories().isEmpty())
                .collect(Collectors.toList());

        return filtered.stream()
                .map(o -> StudentOrgProfileListResponse.builder()
                        .studentOrgId(o.getStudentOrgId())
                        .schoolName(o.getSchoolName())
                        .organizationName(o.getOrganizationName())
                        .logoUrl(o.getLogoUrl())
                        .verificationLevel(o.getVerificationLevel())
                        .safetyFlag(o.getSafetyFlag())
                        .collaborationType(o.getCollaborationType())
                        .organizationType(o.getOrganizationType())
                        .collaborationCount(o.getHistories().size())
                        .build())
                .collect(Collectors.toList());
    }

    /** 학생단체 상세 조회 */
    public StudentOrgProfileDetailResponse getProfileDetail(Long studentOrgId) {
        StudentOrg org = studentOrgRepository.findById(studentOrgId)
                .orElseThrow(() -> new IllegalArgumentException("해당 학생 단체를 찾을 수 없습니다."));

        List<StudentOrgProfileDetailResponse.ContactDto> contacts =
                (org.getContacts() == null ? List.<StudentOrgContact>of() : org.getContacts())
                        .stream()
                        .map(c -> StudentOrgProfileDetailResponse.ContactDto.builder()
                                .name(c.getName())
                                .phone(c.getPhone())
                                .email(c.getEmail())
                                .build())
                        .collect(Collectors.toList());

        List<StudentOrgProfileDetailResponse.EventDto> events =
                (org.getAvailabilities() == null ? List.<StudentOrgAvailability>of() : org.getAvailabilities())
                        .stream()
                        .map(a -> StudentOrgProfileDetailResponse.EventDto.builder()
                                .availabilityId(a.getAvailabilityId())
                                .eventName(a.getEventName())
                                .hostName(a.getHostName())
                                .place(a.getPlace())
                                .eventType(a.getEventType())
                                .description(a.getDescription())
                                .targetAgeRange(a.getTargetAgeRange())
                                .targetMajor(a.getTargetMajor())
                                .targetInterest(a.getTargetInterest())
                                .exposureCount(a.getExposureCount())
                                .recommendedSampleQty(a.getRecommendedSampleQty())
                                .promotionProcess(a.getPromotionProcess())
                                .eventPoints(a.getEventPoints())
                                .promotionPlan(a.getPromotionPlan())
                                .efficiencyMetric(a.getEfficiencyMetric())
                                .startDate(a.getStartDate() != null ? a.getStartDate().toString() : null)
                                .endDate(a.getEndDate() != null ? a.getEndDate().toString() : null)
                                .build())
                        .collect(Collectors.toList());

        List<StudentOrgHistory> histories = historyRepository.findByStudentOrg_StudentOrgId(studentOrgId);
        List<StudentOrgProfileDetailResponse.HistoryDto> historyDtos = histories.stream()
                .map(h -> StudentOrgProfileDetailResponse.HistoryDto.builder()
                        .historyId(h.getHistoryId())
                        .campaignId(h.getCampaignId())
                        .role(h.getRole() != null ? h.getRole().name() : null)
                        .status(h.getStatus() != null ? h.getStatus().name() : null)
                        .build())
                .collect(Collectors.toList());

        return StudentOrgProfileDetailResponse.builder()
                .studentOrgId(org.getStudentOrgId())
                .schoolName(org.getSchoolName())
                .organizationName(org.getOrganizationName())
                .logoUrl(org.getLogoUrl())
                .verificationLevel(org.getVerificationLevel())
                .safetyFlag(org.getSafetyFlag())
                .contacts(contacts)
                .events(events)
                .histories(historyDtos)
                .build();
    }

    /** 학생단체 일정 캘린더 */
    public List<StudentOrgCalendarResponse> getCalendar(Long studentOrgId) {
        YearMonth thisMonth = YearMonth.now();
        LocalDate start = thisMonth.minusMonths(1).atDay(1);
        LocalDate end = thisMonth.plusMonths(3).atEndOfMonth();

        List<StudentOrgAvailability> availabilities =
                availabilityRepository.findCalendar(studentOrgId, start, end);

        return availabilities.stream()
                .map(a -> StudentOrgCalendarResponse.builder()
                        .availabilityId(a.getAvailabilityId())
                        .eventName(a.getEventName())
                        .startDate(a.getStartDate() != null ? a.getStartDate().toString() : null)
                        .endDate(a.getEndDate() != null ? a.getEndDate().toString() : null)
                        .place(a.getPlace())
                        .build())
                .collect(Collectors.toList());
    }
}
