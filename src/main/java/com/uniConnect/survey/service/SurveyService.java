package com.uniConnect.survey.service;

import com.uniConnect.studentOrg.entity.StudentOrg;
import com.uniConnect.studentOrg.repository.StudentOrgRepository;
import com.uniConnect.survey.dto.SurveyRequestDto;
import com.uniConnect.survey.dto.SurveyResponseDto;
import com.uniConnect.survey.entity.Survey;
import com.uniConnect.survey.entity.SurveyStatus;
import com.uniConnect.survey.repository.SurveyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SurveyService {

    private final SurveyRepository surveyRepository;
    private final StudentOrgRepository studentOrgRepository;

    // 설문 생성
    public SurveyResponseDto createSurvey(SurveyRequestDto dto) {
        StudentOrg org = studentOrgRepository.findById(dto.getStudentOrgId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 단체를 찾을 수 없습니다."));

        Survey survey = Survey.builder()
                .studentOrg(org)
                .title(dto.getTitle())
                .description(dto.getDescription())
                .externalLink(dto.getExternalLink())
                .status(SurveyStatus.PENDING)
                .build();

        surveyRepository.save(survey);
        return SurveyResponseDto.fromEntity(survey);
    }

    // 전체 설문 조회 (관리자용)
    public List<SurveyResponseDto> getAllSurveys() {
        return surveyRepository.findAll().stream()
                .map(SurveyResponseDto::fromEntity)
                .toList();
    }

    // 특정 단체의 설문 조회
    public List<SurveyResponseDto> getSurveysByOrg(Long orgId) {
        StudentOrg org = studentOrgRepository.findById(orgId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 단체를 찾을 수 없습니다."));
        return surveyRepository.findByStudentOrg(org).stream()
                .map(SurveyResponseDto::fromEntity)
                .toList();
    }

    // 설문 상세 조회
    public SurveyResponseDto getSurvey(Long id) {
        Survey s = surveyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "설문을 찾을 수 없습니다."));
        return SurveyResponseDto.fromEntity(s);
    }

    // 설문 링크 반환 (구글폼 이동)
    public String getExternalLink(Long id) {
        Survey s = surveyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "설문을 찾을 수 없습니다."));
        return s.getExternalLink();
    }

    // 설문 상태 변경 (예: PENDING → ACTIVE or CLOSED)

    public SurveyResponseDto updateStatus(Long id, SurveyStatus status) {
        Survey s = surveyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "설문을 찾을 수 없습니다."));
        s.setStatus(status);
        surveyRepository.save(s);
        return SurveyResponseDto.fromEntity(s);
    }
}

