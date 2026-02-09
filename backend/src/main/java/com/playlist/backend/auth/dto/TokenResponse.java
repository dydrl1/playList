package com.playlist.backend.auth.dto;

import com.playlist.backend.user.User;
import com.playlist.backend.user.dto.UserInfo;

public class TokenResponse {

    private final String accessToken;
    private final String refreshToken;
    private UserInfo userInfo;

    public TokenResponse(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }


    public UserInfo getUserInfo() { return userInfo; }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }
}
