package com.example.sca_be.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDeleteRequest {

    @NotBlank(message = "비밀번호는 필수입니다.")
    private String password;
}





