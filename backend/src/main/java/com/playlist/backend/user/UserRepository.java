package com.playlist.backend.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // 로그인 시 email로 유저 조회
    Optional<User> findByEmail(String email); // Optional로 반환 타입 둔 이유 : NPE 위험 X, 예외처리 깔끔

    // 회원가입 시 email 중복 체크
    boolean existsByEmail(String email);
}