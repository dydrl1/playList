package com.playlist.backend.playlist;

import com.playlist.backend.playlistTrack.PlaylistTrack;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PlaylistRepository extends JpaRepository<Playlist, Long> {

    // 내 플레이리스트들
    List<Playlist> findAllByUserId(Long userId);

    // 소유자 검증까지 포함해서 찾을 때
    Optional<Playlist> findByIdAndUserId(Long id, Long userId);

    @Query("""
        select pt
        from PlaylistTrack pt
        join fetch pt.track t
        where pt.playlist.id = :playlistId
        order by pt.trackOrder asc
    """)
    List<PlaylistTrack> findAllWithTrackByPlaylistId(@Param("playlistId") Long playlistId);

    // 내 플레이리스트 최신순 조회
    List<Playlist> findAllByUserIdOrderByIdDesc(Long userId);

    // 최신순 플레이리스트 전체조회 정렬
    Page<Playlist> findByIsPublicTrueOrderByIdDesc(Pageable pageable);
    // 조회수 동일할 때 최신이 위로 오게 안정 정렬(추천)
    Page<Playlist> findByIsPublicTrueOrderByViewCountDescIdDesc(Pageable pageable);
     // 좋아요순 플레이리스트 전체조회 정렬
    @Query(
            value = """
        select p
        from Playlist p
        left join PlaylistLike pl on pl.playlist = p
        where p.isPublic = true
        group by p
        order by count(pl.id) desc, p.id desc
      """,
            countQuery = """
        select count(p)
        from Playlist p
        where p.isPublic = true
      """
    )
    Page<Playlist> findPublicOrderByLikeCountDesc(Pageable pageable);

    // 조회수 증가 쿼리
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Playlist p set p.viewCount = p.viewCount + 1 where p.id = :playlistId")
    int increaseViewCount(@Param("playlistId") Long playlistId);


}
