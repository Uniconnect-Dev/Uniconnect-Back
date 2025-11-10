package com.uniConnect.collaboration.dto;

import com.uniConnect.collaboration.entity.ContentUpload;
import com.uniConnect.collaboration.entity.StudentReceiveInfo;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentSectionDto {

    // 수령인 정보
    private String receiverName;
    private String receivePlace;

    // 학생단체가 업로드한 카드뉴스들
    private List<CardNewsBlock> cardNews;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CardNewsBlock {
        private String imageUrl;
        private String caption;
        private LocalDateTime uploadedAt;
    }

    public static StudentSectionDto of(StudentReceiveInfo info, List<ContentUpload> uploads) {
        return StudentSectionDto.builder()
                .receiverName(info != null ? info.getReceiverName() : null)
                .receivePlace(info != null ? info.getReceivePlace() : null)
                .cardNews(
                        uploads.stream()
                                // 학생단체가 올린 것만
                                .filter(u -> u.getUploaderType() != null && u.getUploaderType().name().equals("StudentOrg"))
                                .map(u -> CardNewsBlock.builder()
                                        .imageUrl(u.getImageUrl())
                                        .caption(u.getCaption())
                                        .uploadedAt(u.getUploadedAt())
                                        .build()
                                ).toList()
                )
                .build();
    }
}