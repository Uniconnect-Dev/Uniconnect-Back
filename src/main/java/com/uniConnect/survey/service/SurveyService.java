package com.uniConnect.survey.service;

import com.uniConnect.member.entity.LocalCredential;
import com.uniConnect.member.entity.User;
import com.uniConnect.member.enums.UserRole;
import com.uniConnect.member.repository.UserRepository;
import com.uniConnect.company.repository.CompanyRepository;
import com.uniConnect.company.entity.Company;
import com.uniConnect.member.security.local.CustomUser;
import com.uniConnect.member.repository.LocalCredentialRepository;
import com.uniConnect.studentOrg.entity.StudentOrg;
import com.uniConnect.studentOrg.repository.StudentOrgRepository;
import com.uniConnect.survey.dto.*;
import com.uniConnect.survey.entity.*;
import com.uniConnect.survey.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SurveyService {

    private final SurveyRepository surveyRepository;
    private final StudentOrgRepository studentOrgRepository;
    private final LocalCredentialRepository localCredentialRepository;
    private final SurveyResponseRepository surveyResponseRepository;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;



    /**
     * 로그인한 단체(JWT loginId 기반)로 설문 생성
     */
    public SurveyResponseDto createSurveyByJwt(SurveyRequestDto dto) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUser customUser = (CustomUser) authentication.getPrincipal();
        Long userId = customUser.getUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        if (user.getRole() != UserRole.Company) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "기업만 설문을 생성할 수 있습니다.");
        }

        Company company = companyRepository.findByUsers_UserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "기업 정보를 찾을 수 없습니다."));

        StudentOrg org = null;
        if (dto.getStudentOrgId() != null) {
            org = studentOrgRepository.findById(dto.getStudentOrgId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "학생단체를 찾을 수 없습니다."));
        }

        Survey survey = Survey.builder()
                .company(company)
                .studentOrg(org)
                .title(dto.getTitle())
                .description(dto.getDescription())
                .externalLink(dto.getExternalLink())
                .status(SurveyStatus.Pending)
                .build();

        surveyRepository.save(survey);

        return SurveyResponseDto.fromEntity(survey);
    }

    /**
     * loginId로 orgId 조회
     */
    public Long findOrgIdByUserId(Long userId) {

        StudentOrg org = studentOrgRepository.findByUsers_UserId(userId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 단체를 찾을 수 없습니다.")
                );

        return org.getStudentOrgId();
    }


    /**
     * 전체 설문 조회 (관리자용)
     */
    public List<SurveyResponseDto> getAllSurveys() {
        return surveyRepository.findAll().stream().map(SurveyResponseDto::fromEntity).toList();
    }

    /**
     * 특정 단체의 설문 조회
     */
    public List<SurveyResponseDto> getSurveysByOrg(Long orgId) {
        StudentOrg org = studentOrgRepository.findById(orgId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 단체를 찾을 수 없습니다."));
        return surveyRepository.findByStudentOrg(org).stream().map(SurveyResponseDto::fromEntity).toList();
    }

    /**
     * 설문 상세 조회
     */
    public SurveyResponseDto getSurvey(Long id) {
        Survey s = surveyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "설문을 찾을 수 없습니다."));
        return SurveyResponseDto.fromEntity(s);
    }

    /**
     * 설문 외부 링크 조회
     */
    public String getExternalLink(Long id) {
        Survey s = surveyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "설문을 찾을 수 없습니다."));
        return s.getExternalLink();
    }

    /**
     * 설문 상태 업데이트
     */
    public SurveyResponseDto updateStatus(Long id, SurveyStatus status) {
        Survey s = surveyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "설문을 찾을 수 없습니다."));
        s.setStatus(status);
        surveyRepository.save(s);
        return SurveyResponseDto.fromEntity(s);
    }

    /**
     * 설문 수정
     */
    public SurveyResponseDto updateSurvey(Long id, SurveyUpdateRequestDto dto) {
        Survey survey = surveyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (dto.getTitle() != null) survey.setTitle(dto.getTitle());
        if (dto.getDescription() != null) survey.setDescription(dto.getDescription());
        if (dto.getExternalLink() != null) survey.setExternalLink(dto.getExternalLink());

        surveyRepository.save(survey);
        return SurveyResponseDto.fromEntity(survey);
    }

    /**
     * 응답 추가
     */
    public SurveyResponse addResponse(Long surveyId, SurveyResponseRequestDto dto) {
        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        SurveyResponse response = SurveyResponse.builder()
                .survey(survey)
                .fileUrl(dto.getFileUrl())
                .summaryText(dto.getSummaryText())
                .build();

        return surveyResponseRepository.save(response);
    }

    /**
     * 기업이 설문 응답 목록 조회
     */
    public List<SurveyResponse> getResponses(Long surveyId, boolean masking) {
        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        List<SurveyResponse> responses = surveyResponseRepository.findBySurvey(survey);

        if (masking) {
            responses.forEach(r -> {
                String text = r.getSummaryText();
                if (text != null && text.length() > 2) {
                    r.setSummaryText(text.substring(0, 2) + "***");
                }
            });
        }

        return responses;
    }

    public Survey getSurveyEntity(Long id) {
        return surveyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

}
