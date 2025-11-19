package com.uniConnect.qna.dto;

import java.util.List;
import com.uniConnect.qna.enums.*;

public record QnaCreateRequest(
        QnaType type,       // COMPANY or STUDENT_ORG
        Long companyId,     // 선택적
        Long studentOrgId,  // 선택적
        String title,
        String content,
        String password,
        Boolean agreePersonalInfo,
        Boolean agreeNotification,
        List<String> fileUrls,
        List<String> originalNames
) {}