package com.playlist.backend.api;

import com.playlist.backend.dto.request.LoginRequest;
import com.playlist.backend.dto.request.SignupRequest;
import com.playlist.backend.entity.User;
import com.playlist.backend.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // 회원가입
    @PostMapping("/signup")
    public User signup(@RequestBody SignupRequest request) {
        return userService.register(request.getName(), request.getEmail(), request.getPassword());
    }

    // 로그인
    @PostMapping("/login")
    public User login(@RequestBody LoginRequest request) {
        return userService.login(request.getEmail(), request.getPassword());
    }
}
