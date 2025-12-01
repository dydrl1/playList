package com.playlist.backend.user.dto;

import lombok.Getter;

@Getter
public class UserProfileUpdateRequest {
    
    private String name; // 일단 이름만 수정하도록, 후에 추가 가능
}
