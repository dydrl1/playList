package com.playlist.backend.repository;

import com.playlist.backend.entity.PlaylistLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaylistLikeRepository extends JpaRepository<PlaylistLike, Long> {

    // 유저가 이미 해당 플레이리스트에 좋아요 했는지 체크
    boolean existsByUserIdAndPlaylistId(Long userId, Long playlistId);

    // 플레이리스트 좋아요 개수
    long countByPlaylistId(Long playlistId);

    // 좋아요 취소할 때 사용
    void deleteByUserIdAndPlaylistId(Long userId, Long playlistId);
}
