package com.playlist.backend.auth;

import com.playlist.backend.auth.dto.LoginRequest;
import com.playlist.backend.auth.dto.LoginResponse;
import com.playlist.backend.auth.dto.SignupRequest;
import com.playlist.backend.auth.dto.TokenResponse;
import com.playlist.backend.auth.token.dto.RefreshRequest;
import com.playlist.backend.common.response.ApiResponse;
import com.playlist.backend.security.JwtUtil;
import com.playlist.backend.user.User;
import jakarta.validation.Valid;
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
    public ResponseEntity<ApiResponse<String>> signup(@RequestBody @Valid SignupRequest request) {
        authService.register(request.getName(), request.getEmail(), request.getPassword());
        return ResponseEntity.ok(
                ApiResponse.success("회원가입이 완료되었습니다.")
        );
    }


    // 로그인
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(
            @RequestBody @Valid LoginRequest request
    ) {
        TokenResponse tokenResponse =
                authService.login(request.getEmail(), request.getPassword());

        ApiResponse<TokenResponse> body = ApiResponse.success(tokenResponse);

        return jwtUtil.withBearerHeader(body, tokenResponse.getAccessToken());
    }


    // 리프레쉬 토큰
    @PostMapping("/refresh")
    public TokenResponse refresh(@RequestBody RefreshRequest req) {
        return authService.refresh(req.getRefreshToken());
    }


    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody @Valid RefreshRequest request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.ok().build();
    }
}

