package com.uniConnect.profile.service;

import com.uniConnect.global.exception.CustomException;
import com.uniConnect.global.exception.ErrorCode;
import com.uniConnect.member.entity.User;
import com.uniConnect.member.repository.UserRepository;
import com.uniConnect.profile.dto.StudentOrgGetResponse;
import com.uniConnect.profile.dto.StudentOrgInitRequest;
import com.uniConnect.profile.dto.StudentOrgGetResponse;
import com.uniConnect.profile.dto.StudentOrgUpdateRequest;
import com.uniConnect.studentOrg.entity.StudentOrg;
import com.uniConnect.studentOrg.repository.StudentOrgRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentOrgProfileService {

    private final StudentOrgRepository studentOrgRepository;
    private final UserRepository userRepository;

    @Transactional
    public StudentOrgGetResponse initProfile(Long userId, StudentOrgInitRequest request) {
        if (studentOrgRepository.existsByUsers_UserId(userId)) {
            throw new CustomException(ErrorCode.ALREADY_EXISTS);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 새 StudentOrg 생성
        StudentOrg studentOrg = StudentOrg.builder()
                .schoolName(request.getSchoolName())
                .organizationName(request.getOrganizationName())
                .managerName(request.getManagerName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .logoUrl(request.getLogoUrl())
                .verificationLevel(0)
                .safetyFlag(false)
                .build();

        // 관계 설정 (1:N)
        studentOrg.getUsers().add(user);
        user.setStudentOrg(studentOrg);

        StudentOrg saved = studentOrgRepository.save(studentOrg);

        return toResponse(saved);
    }

    public StudentOrgGetResponse getProfile(Long userId) {
        StudentOrg studentOrg = studentOrgRepository.findByUsers_UserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROFILE_NOT_FOUND));
        return toResponse(studentOrg);
    }

    @Transactional
    public StudentOrgGetResponse updateProfile(Long userId, StudentOrgUpdateRequest request) {
        StudentOrg studentOrg = studentOrgRepository.findByUsers_UserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROFILE_NOT_FOUND));

        if (request.getSchoolName() != null) studentOrg.setSchoolName(request.getSchoolName());
        if (request.getOrganizationName() != null) studentOrg.setOrganizationName(request.getOrganizationName());
        if (request.getManagerName() != null) studentOrg.setManagerName(request.getManagerName());
        if (request.getPhone() != null) studentOrg.setPhone(request.getPhone());
        if (request.getEmail() != null) studentOrg.setEmail(request.getEmail());
        if (request.getLogoUrl() != null) studentOrg.setLogoUrl(request.getLogoUrl());

        return toResponse(studentOrg);
    }

    private StudentOrgGetResponse toResponse(StudentOrg studentOrg) {
        return StudentOrgGetResponse.builder()
                .studentOrgId(studentOrg.getStudentOrgId())
                .schoolName(studentOrg.getSchoolName())
                .organizationName(studentOrg.getOrganizationName())
                .managerName(studentOrg.getManagerName())
                .phone(studentOrg.getPhone())
                .email(studentOrg.getEmail())
                .logoUrl(studentOrg.getLogoUrl())
                .verificationLevel(studentOrg.getVerificationLevel())
                .safetyFlag(studentOrg.getSafetyFlag())
                .build();
    }
}