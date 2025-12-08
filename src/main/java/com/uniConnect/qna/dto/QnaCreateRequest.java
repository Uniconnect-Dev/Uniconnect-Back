package com.uniConnect.qna.dto;

import java.util.List;
import com.uniConnect.qna.enums.*;

public record QnaCreateRequest(
        QnaType type,       // COMPANY or STUDENT_ORG
        String title,
        String content,
        String password,
        Boolean agreePersonalInfo,
        Boolean agreeNotification,
        List<String> fileUrls,
        List<String> originalNames
) {}