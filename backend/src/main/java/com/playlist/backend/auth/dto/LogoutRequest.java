package com.playlist.backend.auth.dto;

import jakarta.validation.constraints.NotBlank;

public class LogoutRequest {

    @NotBlank(message = "리프레시 토큰은 필수입니다.")
    private String refreshToken;

    public String getRefreshToken() {
        return refreshToken;
    }
}
