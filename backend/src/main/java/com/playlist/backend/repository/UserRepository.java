package com.playlist.backend.repository;

import com.playlist.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // 기본 CRUD는 상속만으로 끝
}