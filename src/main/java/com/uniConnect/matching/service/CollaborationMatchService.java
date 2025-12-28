package com.uniConnect.matching.service;

import com.uniConnect.campaign.enums.MatchingStatus;
import com.uniConnect.sampling.enums.IndustryType;
import com.uniConnect.matching.entity.CollaborationMatchRequest;
import com.uniConnect.matching.dto.MatchingCompleteRequest;
import com.uniConnect.matching.dto.MatchingCompleteResponse;
import com.uniConnect.matching.repository.CollaborationMatchRequestRepository;
import com.uniConnect.matching.dto.CompanyMatchRequest;
import com.uniConnect.matching.dto.CompanyMatchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CollaborationMatchService {

    private final CollaborationMatchRequestRepository collaborationMatchRequestRepository;

    /**
     * 매칭 버튼 마무리: 선택된 기업들에게 매칭 요청 여러 개 생성
     */
    @Transactional
    public MatchingCompleteResponse completeMatching(Long studentOrgId, MatchingCompleteRequest dto) {

        List<CollaborationMatchRequest> saved = new ArrayList<>();

        for (Long companyId : dto.getSelectedCompanyIds()) {

            CollaborationMatchRequest req = CollaborationMatchRequest.builder()
                    .studentOrgId(studentOrgId)
                    .companyId(companyId)
                    .eventTitle(dto.getEventTitle())
                    .desiredDate(dto.getDesiredDate())
                    .industry(IndustryType.valueOf(dto.getIndustry()))
                    .collaborationType(dto.getCollaborationType())
                    .status(MatchingStatus.Requested)
                    .requestedAt(LocalDateTime.now())
                    .build();

            saved.add(collaborationMatchRequestRepository.save(req));
        }

        return MatchingCompleteResponse.builder()
                .total(saved.size())
                .message("선택하신 기업들에게 매칭 요청이 정상적으로 제출되었습니다.")
                .build();
    }

    @Transactional
    public CompanyMatchResponse completeCompanyMatching(Long companyId, CompanyMatchRequest dto) {

        List<CollaborationMatchRequest> saved = new ArrayList<>();

        for (Long studentOrgId : dto.getTargetStudentOrgIds()) {

            CollaborationMatchRequest req = CollaborationMatchRequest.builder()
                    .studentOrgId(studentOrgId)
                    .companyId(companyId)
                    .eventTitle(dto.getEventTitle())
                    .desiredDate(dto.getDesiredDate())
                    .industry(IndustryType.valueOf(dto.getIndustry()))
                    .collaborationType(dto.getCollaborationType())
                    .status(MatchingStatus.Requested)
                    .requestedAt(LocalDateTime.now())
                    .build();

            saved.add(collaborationMatchRequestRepository.save(req));
        }

        return CompanyMatchResponse.builder()
                .total(saved.size())
                .message("선택한 학생단체들에게 매칭 요청이 정상적으로 제출되었습니다.")
                .build();
    }
}