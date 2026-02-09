package com.playlist.backend.auth;

import com.playlist.backend.auth.dto.LoginRequest;
import com.playlist.backend.auth.dto.LoginResponse;
import com.playlist.backend.auth.dto.SignupRequest;
import com.playlist.backend.auth.dto.TokenResponse;
import com.playlist.backend.auth.token.dto.RefreshRequest;
import com.playlist.backend.common.response.ApiResponse;
import com.playlist.backend.security.JwtUtil;
import com.playlist.backend.user.User;
import com.playlist.backend.user.UserRepository;
import com.playlist.backend.user.dto.UserInfo;
import com.playlist.backend.user.dto.UserProfileResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;


    public AuthController(AuthService authService,
                          JwtUtil jwtUtil, UserRepository userRepository) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
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
    public ResponseEntity<UserInfo> login(@RequestBody LoginRequest request) {
        TokenResponse tokenResponse = authService.login(request.getEmail(), request.getPassword());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserInfo userInfo = UserInfo.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();

        return ResponseEntity.ok()
                .header("Authorization", "Bearer " + tokenResponse.getAccessToken())
                .header("Refresh-Token", tokenResponse.getRefreshToken())
                .body(userInfo);
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

