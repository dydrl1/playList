package com.playlist.backend.auth;

import com.playlist.backend.auth.dto.LoginRequest;
import com.playlist.backend.auth.dto.LoginResponse;
import com.playlist.backend.auth.dto.SignupRequest;
import com.playlist.backend.common.response.ApiResponse;
import com.playlist.backend.security.JwtUtil;
import com.playlist.backend.user.User;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<ApiResponse<String>> signup(@RequestBody SignupRequest request) {
        authService.register(request.getName(), request.getEmail(), request.getPassword());
        return ResponseEntity.ok(
                ApiResponse.success("회원가입이 완료되었습니다.")
        );
    }


    // 로그인
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest request) {
        User user = authService.login(request.getEmail(), request.getPassword());

        String accessToken = jwtUtil.generateAccessToken(user.getEmail());

        LoginResponse response = LoginResponse.builder()
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();

        //  body는 ApiResponse로 감싸고, 헤더는 지금처럼 jwtUtil로 처리
        ApiResponse<LoginResponse> body = ApiResponse.success(response);

        return jwtUtil.withBearerHeader(body, accessToken);
    }
}

