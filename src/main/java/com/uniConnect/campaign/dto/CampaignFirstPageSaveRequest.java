package com.uniConnect.campaign.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class CampaignFirstPageSaveRequest {

    @NotBlank
    private String managerName;

    @NotBlank
    private String managerPhone;

    @Email
    @NotBlank
    private String managerEmail;
}
