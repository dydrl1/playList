package com.playlist.backend.auth;

import com.playlist.backend.auth.dto.LoginRequest;
import com.playlist.backend.auth.dto.LoginResponse;
import com.playlist.backend.auth.dto.SignupRequest;
import com.playlist.backend.security.CustomUserDetails;
import com.playlist.backend.security.JwtTokenProvider;
import com.playlist.backend.security.JwtUtil;
import com.playlist.backend.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;


    public AuthController(AuthService authService,
                          JwtUtil jwtUtil) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
    }

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<Void> signup(@RequestBody SignupRequest request) {
        authService.register(request.getName(), request.getEmail(), request.getPassword());
        return ResponseEntity.ok().build();
    }


    // 로그인
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        User user = authService.login(request.getEmail(), request.getPassword());

        String accessToken = jwtUtil.generateAccessToken(user.getEmail());

        LoginResponse response = LoginResponse.builder()
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();

        return jwtUtil.withBearerHeader(response, accessToken);
    }

}
