package com.playlist.backend.playlistLike;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PlaylistLikeRepository extends JpaRepository<PlaylistLike, Long> {

    // 내가 좋아요 눌렀는지
    boolean existsByPlaylistIdAndUserId(Long playlistId, Long userId);

    // 플레이리스트 좋아요 개수
    long countByPlaylistId(Long playlistId);

    // 집계 쿼리 추가
    @Query("""
        select pl.playlist.id as playlistId, count(pl) as cnt
        from PlaylistLike pl
        where pl.playlist.id in :playlistIds
        group by pl.playlist.id
    """)
    List<PlaylistLikeCountRow> countGroupByPlaylistIds(@Param("playlistIds") List<Long> playlistIds);

    // 좋아요 엔티티 가져오기
    Optional<PlaylistLike> findByUserIdAndPlaylistId(Long userId, Long playlistId);
}
