package com.uniConnect.member.security.local.dto;

import com.uniConnect.member.enums.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LocalLoginReq(@NotBlank @Size(max = 50)
                           String loginId,

                            @NotBlank @Size(max = 100)
                           String password) {
}
