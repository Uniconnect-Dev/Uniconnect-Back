package com.uniConnect.collaboration.dto;

import com.uniConnect.collaboration.entity.StudentReceiveInfo;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StudentReceiveInfoResponse {

    private Long id;
    private String receiverName;
    private String receivePlace;
    private String note;

    public static StudentReceiveInfoResponse from(StudentReceiveInfo info) {
        return StudentReceiveInfoResponse.builder()
                .id(info.getId())
                .receiverName(info.getReceiverName())
                .receivePlace(info.getReceivePlace())
                .note(info.getNote())
                .build();
    }
}
