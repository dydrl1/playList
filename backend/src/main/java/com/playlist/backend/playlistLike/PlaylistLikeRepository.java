package com.playlist.backend.playlistLike;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaylistLikeRepository extends JpaRepository<PlaylistLike, Long> {

    // 내가 좋아요 눌렀는지
    boolean existsByPlaylistIdAndUserId(Long playlistId, Long userId);

    // 플레이리스트 좋아요 개수
    long countByPlaylistId(Long playlistId);

    // 좋아요 취소할 때 사용
    void deleteByUserIdAndPlaylistId(Long userId, Long playlistId);
}
