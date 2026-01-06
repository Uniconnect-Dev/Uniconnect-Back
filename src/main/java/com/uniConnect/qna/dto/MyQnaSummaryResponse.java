package com.uniConnect.qna.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MyQnaSummaryResponse {

    private long totalCount;
    private long pendingCount;
    private long answeredCount;

    private List<QuestionDetailResponse> questions;
}
