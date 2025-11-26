package com.playlist.backend.service;

import com.playlist.backend.entity.User;
import com.playlist.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 회원가입
     */
    public User register(String name, String email, String rawPassword) {

        // 이메일 중복 검증
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        // (암호화 X 버전) → 후에 BCrypt 적용 예정
        User user = new User(name, email, rawPassword);

        return userRepository.save(user);
    }

    /**
     * 로그인
     */
    public User login(String email, String rawPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    throw new IllegalArgumentException("존재하지 않는 이메일입니다.");
                });

        // 비밀번호 검증
        if (!user.getPassword().equals(rawPassword)) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        return user;
    }
}
