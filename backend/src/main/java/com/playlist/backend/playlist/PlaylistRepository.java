package com.playlist.backend.playlist;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlaylistRepository extends JpaRepository<Playlist, Long> {

    // 내 플레이리스트들
    List<Playlist> findAllByUserId(Long userId);

    // 소유자 검증까지 포함해서 찾을 때
    Optional<Playlist> findByIdAndUserId(Long id, Long userId);
}
