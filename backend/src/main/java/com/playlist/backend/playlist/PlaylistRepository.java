package com.playlist.backend.playlist;

import com.playlist.backend.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlaylistRepository extends JpaRepository<Playlist, Long> {

    // 특정 유저가 만든 플레이리스트 목록
    List<Playlist> findByUser(User user);

    // userId 기준으로 조회
    List<Playlist> findByUserId(Long userId);

    // 공개 플레이리스트만 조회
    List<Playlist> findByIsPublicTrue();
}
