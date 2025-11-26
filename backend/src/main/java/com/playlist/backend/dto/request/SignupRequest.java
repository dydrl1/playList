package com.playlist.backend.dto.request;


public class SignupRequest {
    private String name;
    private String email;
    private String password;

    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
}