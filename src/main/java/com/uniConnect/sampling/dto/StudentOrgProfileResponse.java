package com.uniConnect.sampling.dto.response;

public record StudentOrgProfileResponse(
        String schoolName,
        String orgName,
        String contactName,
        String phone,
        String email
) {}
