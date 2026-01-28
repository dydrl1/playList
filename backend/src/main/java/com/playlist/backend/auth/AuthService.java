package com.playlist.backend.auth;

import com.playlist.backend.auth.dto.TokenResponse;
import com.playlist.backend.auth.token.RefreshToken;
import com.playlist.backend.auth.token.RefreshTokenHasher;
import com.playlist.backend.auth.token.RefreshTokenRepository;
import com.playlist.backend.common.exception.BusinessException;
import com.playlist.backend.common.exception.ErrorCode;
import com.playlist.backend.security.JwtUtil;
import com.playlist.backend.user.User;
import com.playlist.backend.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

import static com.playlist.backend.common.validator.EmailValidator.isValid;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;
    private final RefreshTokenHasher refreshTokenHasher;


    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       RefreshTokenRepository refreshTokenRepository,
                       JwtUtil jwtUtil, RefreshTokenHasher refreshTokenHasher) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtUtil = jwtUtil;
        this.refreshTokenHasher = refreshTokenHasher;
    }

    /**
     * 회원가입
     */
    public User register(String name, String email, String rawPassword) {

        // 이메일 형식 검증
        if (!isValid(email)){
            throw new BusinessException(ErrorCode.INVALID_EMAIL_FORMAT);
        }

        // 이메일 중복 검증
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.EMAIL_DUPLICATED);
        }

        // 비밀번호 암호화 (BCrypt)
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // User 엔티티 생성
        User user = new User(name, email, encodedPassword);

        return userRepository.save(user);
    }

    /**
     * 로그인
     */
    public TokenResponse login(String email, String rawPassword) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new BusinessException(ErrorCode.AUTHENTICATION_FAILED);
        }

        String accessToken = jwtUtil.generateAccessToken(user.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        String tokenHash = refreshTokenHasher.hash(refreshToken);
        refreshTokenRepository.save(
                RefreshToken.issue(user.getId(), tokenHash, LocalDateTime.now().plusDays(14))
        );

        return new TokenResponse(accessToken, refreshToken);
    }


    // 토큰 재발급
    public TokenResponse refresh(String refreshToken) {

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID);
        }

        jwtUtil.validateTokenOrThrow(refreshToken); // 만료면 TOKEN_EXPIRED, 그 외 INVALID_TOKEN

        if (!jwtUtil.isRefreshToken(refreshToken)) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID);
        }

        String username = jwtUtil.getUsernameFromToken(refreshToken);

        // DB 검증(해시로 조회)
        String tokenHash = refreshTokenHasher.hash(refreshToken);
        RefreshToken saved = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID));

        if (saved.isExpired()) {
            saved.revoke();
            refreshTokenRepository.save(saved);
            throw new BusinessException(ErrorCode.TOKEN_EXPIRED);
        }

        if (saved.isRevoked()) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID);
        }

        saved.markUsed();
        refreshTokenRepository.save(saved);

        String newAccessToken = jwtUtil.generateAccessToken(username);

        return new TokenResponse(newAccessToken, refreshToken);
    }
    
    // 로그아웃
    @Transactional
    public void logout(String refreshToken) {

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID);
        }

        // 1) JWT 자체 검증 (만료/위조 구분)
        // - 만료면 TOKEN_EXPIRED
        // - 위조/형식 오류면 INVALID_TOKEN
        jwtUtil.validateTokenOrThrow(refreshToken);

        // 2) Refresh 토큰인지 확인 (typ=REFRESH)
        if (!jwtUtil.isRefreshToken(refreshToken)) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID);
        }

        // 3) DB 저장값 revoke 처리
        String tokenHash = refreshTokenHasher.hash(refreshToken);

        RefreshToken saved = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID));

        // 이미 만료/폐기된 토큰으로 로그아웃 요청 시 정책 선택:
        // - 엄격하게 에러로 처리하려면 아래처럼
        if (saved.isExpired()) {
            saved.revoke();
            throw new BusinessException(ErrorCode.TOKEN_EXPIRED);
        }
        if (saved.isRevoked()) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID);
        }

        saved.revoke();
        refreshTokenRepository.save(saved);
    }

}
