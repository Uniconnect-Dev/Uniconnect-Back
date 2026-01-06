package com.uniConnect.partnership.dto;

import com.uniConnect.partnership.enums.PartnershipType;
import com.uniConnect.partnership.enums.CollaborationPeriodType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.Map;
import java.time.LocalDate;

@Getter
@Schema(description = "기업 협업 제안 생성 요청 DTO")
public class CollaborationProposalCreateRequest {

    /* =========================
       제휴 분류
    ========================= */

    @Schema(
            description = "제휴 유형",
            example = "Discount"
    )
    private PartnershipType proposalType; // Discount, Sponsorship 등

    /* =========================
       담당자 정보
    ========================= */

    @Schema(example = "김마케팅")
    private String contactName;

    @Schema(example = "010-1234-5678")
    private String contactPhone;

    @Schema(example = "marketing@brand.co.kr")
    private String contactEmail;

    /* =========================
       제휴 정보
    ========================= */

    @Schema(
            description = "제공 상품 또는 서비스명",
            example = "브랜드 신제품 에너지바"
    )
    private String productOrServiceName;

    @Schema(
            description = "산업군",
            example = "식품"
    )
    private String industry;

    @Schema(
            description = "협업 기간 유형",
            example = "Fixed"
    )
    private CollaborationPeriodType periodType; // Always / Fixed

    @Schema(
            description = "협업 시작일 (periodType=Fixed일 때 필수)",
            example = "2026-03-01"
    )
    private LocalDate startDate;

    @Schema(
            description = "협업 종료일 (periodType=Fixed일 때 필수)",
            example = "2026-06-30"
    )
    private LocalDate endDate;

    /* =========================
       제안 내용
    ========================= */

    @Schema(
            description = "협업 제안 상세 내용",
            example = "대학생 대상 장기 할인 및 체험 이벤트 협업을 제안드립니다."
    )
    private String proposalContent;

    @Schema(
            description = "첨부 파일 URL (파일 업로드 API 사용 후 세팅)",
            example = "https://bucket.s3.ap-northeast-2.amazonaws.com/collaboration-proposals/1_proposal.pdf",
            nullable = true
    )
    private String attachmentUrl;

    /* =========================
       JSON 데이터
    ========================= */

    @Schema(
            description = "협업 방식 JSON (최대 5개)",
            example = """
        {
          "offline": {
            "booth": true,
            "sampling": true
          },
          "online": {
            "sns": ["instagram", "youtube"],
            "contentType": "review"
          }
        }
        """
    )
    private Map<String, Object> collaborationMethodsJson;

    @Schema(
            description = "기대 성과 JSON (최대 5개)",
            example = """
        {
          "target": "대학생",
          "expectedReach": 5000,
          "kpi": ["brand_awareness", "trial_conversion"]
        }
        """
    )
    private Map<String, Object> expectedOutcomesJson;

    /* =========================
       동의
    ========================= */

    @Schema(
            description = "개인정보 처리 동의",
            example = "true"
    )
    private Boolean agreePrivacy;

    @Schema(
            description = "마케팅 활용 동의",
            example = "true"
    )
    private Boolean agreeMarketing;
}
