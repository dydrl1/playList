package com.playlist.backend.api;

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

    // ===== DTO =====
    public static class SignupRequest {
        private String name;
        private String email;
        private String password;

        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getPassword() { return password; }
    }

    public static class LoginRequest {
        private String email;
        private String password;

        public String getEmail() { return email; }
        public String getPassword() { return password; }
    }
}
