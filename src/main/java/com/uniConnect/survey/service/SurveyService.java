package com.uniConnect.survey.service;

import com.uniConnect.member.security.local.CustomUser;
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

    public SurveyResponseDto createSurveyByJwt(CustomUser user, SurveyRequestDto dto) {
        StudentOrg org = studentOrgRepository.findById(user.getUsersId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 단체를 찾을 수 없습니다."));

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

    public List<SurveyResponseDto> getAllSurveys() {
        return surveyRepository.findAll().stream().map(SurveyResponseDto::fromEntity).toList();
    }

    public List<SurveyResponseDto> getSurveysByOrg(Long orgId) {
        StudentOrg org = studentOrgRepository.findById(orgId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 단체를 찾을 수 없습니다."));
        return surveyRepository.findByStudentOrg(org).stream().map(SurveyResponseDto::fromEntity).toList();
    }

    public SurveyResponseDto getSurvey(Long id) {
        Survey s = surveyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "설문을 찾을 수 없습니다."));
        return SurveyResponseDto.fromEntity(s);
    }

    public String getExternalLink(Long id) {
        Survey s = surveyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "설문을 찾을 수 없습니다."));
        return s.getExternalLink();
    }

    public SurveyResponseDto updateStatus(Long id, SurveyStatus status) {
        Survey s = surveyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "설문을 찾을 수 없습니다."));
        s.setStatus(status);
        surveyRepository.save(s);
        return SurveyResponseDto.fromEntity(s);
    }
}
