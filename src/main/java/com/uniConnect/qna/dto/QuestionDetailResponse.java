package com.uniConnect.qna.dto;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record QuestionDetailResponse(
        Long questionId,
        String title,
        String content,
        String status,
        List<String> files,
        LocalDateTime createdAt,
        Boolean agreePersonalInfo,
        Boolean agreeNotification,
        String answerContent,
        LocalDateTime answerCreatedAt
) {}