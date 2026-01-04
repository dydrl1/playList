package com.playlist.backend.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class UserProfileResponse {
    private Long id;
    private String name;
    private String email;

    @Builder
    public UserProfileResponse(String name, String email) {
        this.name = name;
        this.email = email;
    }}
