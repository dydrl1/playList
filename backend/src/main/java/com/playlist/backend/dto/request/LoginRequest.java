package com.playlist.backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


public class LoginRequest {

    private String email;
    private String password;

    public String getEmail() { return email; }
    public String getPassword() { return password; }
}
