package com.uniConnect.partnership.service;

import com.uniConnect.s3.S3FileService;
import com.uniConnect.company.entity.Company;
import com.uniConnect.company.repository.CompanyRepository;
import com.uniConnect.member.entity.User;
import com.uniConnect.member.repository.UserRepository;
import com.uniConnect.partnership.dto.CollaborationProposalCreateRequest;
import com.uniConnect.partnership.entity.CollaborationProposal;
import com.uniConnect.partnership.enums.ProposalStatus;
import com.uniConnect.partnership.enums.CollaborationPeriodType;
import com.uniConnect.partnership.repository.CollaborationProposalRepository;
import com.uniConnect.studentOrg.entity.StudentOrg;
import com.uniConnect.studentOrg.repository.StudentOrgRepository;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CollaborationProposalService {

    private final CollaborationProposalRepository proposalRepository;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final S3FileService s3FileService;
    private final ObjectMapper objectMapper;

    @Value("${app.s3.bucket}")
    private String bucketName;

    /**
     * 기업 → 학생단체 협업 제안 생성
     */
    public Long createProposal(Long userId, CollaborationProposalCreateRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Company company = companyRepository.findByUsers_UserId(userId)
                .orElseThrow(() -> new IllegalStateException("기업 계정만 협업 제안이 가능합니다."));

        // 기간 검증
        if (request.getPeriodType() == CollaborationPeriodType.Fixed) {
            if (request.getStartDate() == null || request.getEndDate() == null) {
                throw new IllegalArgumentException("기간 지정 협업은 시작/종료일이 필요합니다.");
            }
        }

        try {
            String collaborationJson =
                    objectMapper.writeValueAsString(request.getCollaborationMethodsJson());

            String expectedJson =
                    objectMapper.writeValueAsString(request.getExpectedOutcomesJson());

            CollaborationProposal proposal = CollaborationProposal.builder()
                    .company(company)

                    .proposalType(request.getProposalType())

                    .contactName(request.getContactName())
                    .contactPhone(request.getContactPhone())
                    .contactEmail(request.getContactEmail())

                    .productOrServiceName(request.getProductOrServiceName())
                    .industry(request.getIndustry())

                    .periodType(request.getPeriodType())
                    .startDate(request.getStartDate())
                    .endDate(request.getEndDate())

                    .proposalContent(request.getProposalContent())
                    .attachmentUrl(request.getAttachmentUrl())

                    .collaborationMethodsJson(collaborationJson)
                    .expectedOutcomesJson(expectedJson)

                    .agreePrivacy(request.getAgreePrivacy())
                    .agreeMarketing(request.getAgreeMarketing())

                    .status(ProposalStatus.Draft)
                    .build();

            proposalRepository.save(proposal);
            return proposal.getProposalId();

        } catch (Exception e) {
            throw new IllegalStateException("협업 제안 JSON 변환 실패", e);
        }
    }

    public void submitProposal(Long userId, Long proposalId) {

        CollaborationProposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new IllegalArgumentException("협업 제안을 찾을 수 없습니다."));

        if (!proposal.getCompany().hasUser(userId)) {
            throw new IllegalStateException("제출 권한이 없습니다.");
        }

        if (proposal.getStatus() != ProposalStatus.Draft) {
            throw new IllegalStateException("이미 제출된 협업 제안입니다.");
        }

        proposal.setStatus(ProposalStatus.Submitted);
    }

    /**
     * 협업 제안서 파일 업로드 (S3)
     */
    public String uploadProposalFile(Long userId, Long proposalId, MultipartFile file) {

        CollaborationProposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new IllegalArgumentException("협업 제안을 찾을 수 없습니다."));

        if (!proposal.getCompany().hasUser(userId)) {
            throw new IllegalStateException("업로드 권한이 없습니다.");
        }

        if (proposal.getStatus() != ProposalStatus.Draft) {
            throw new IllegalStateException("제출된 제안에는 파일을 업로드할 수 없습니다.");
        }

        try {
            String key = "collaboration-proposals/"
                    + proposalId + "_"
                    + file.getOriginalFilename();

            s3FileService.upload(bucketName, key, file);

            String url = "https://" + bucketName
                    + ".s3.ap-northeast-2.amazonaws.com/"
                    + key;

            proposal.setAttachmentUrl(url);
            return url;

        } catch (Exception e) {
            throw new IllegalStateException("제안서 업로드 실패", e);
        }
    }
}
