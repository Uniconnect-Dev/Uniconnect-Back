package com.uniConnect.survey.service;

import com.uniConnect.member.entity.LocalCredential;
import com.uniConnect.member.entity.User;
import com.uniConnect.member.repository.LocalCredentialRepository;
import com.uniConnect.studentOrg.entity.StudentOrg;
import com.uniConnect.studentOrg.repository.StudentOrgRepository;
import com.uniConnect.survey.dto.SurveyRequestDto;
import com.uniConnect.survey.dto.SurveyResponseDto;
import com.uniConnect.survey.entity.Survey;
import com.uniConnect.survey.entity.SurveyStatus;
import com.uniConnect.survey.repository.SurveyRepository;
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

    /**
     * 로그인한 단체(JWT loginId 기반)로 설문 생성
     */
    public SurveyResponseDto createSurveyByJwt(SurveyRequestDto dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String loginId = (String) authentication.getPrincipal();

        // JWT의 loginId로 User 조회
        User user = localCredentialRepository.findByLoginId(loginId)
                .map(LocalCredential::getUser)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 loginId의 사용자를 찾을 수 없습니다."));

        // 해당 유저가 소속된 StudentOrg 조회
        StudentOrg org = studentOrgRepository.findByUsers_UserId(user.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 단체를 찾을 수 없습니다."));

        // 설문 생성
        Survey survey = Survey.builder()
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
    public Long findOrgIdByLoginId(String loginId) {
        User user = localCredentialRepository.findByLoginId(loginId)
                .map(LocalCredential::getUser)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 loginId의 사용자를 찾을 수 없습니다."));

        StudentOrg org = studentOrgRepository.findByUsers_UserId(user.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 단체를 찾을 수 없습니다."));

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
}
