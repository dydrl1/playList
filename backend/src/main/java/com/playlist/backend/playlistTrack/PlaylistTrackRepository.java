package com.playlist.backend.playlistTrack;

import com.playlist.backend.playlist.Playlist;
import com.playlist.backend.playlistLike.PlaylistLikeCountRow;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PlaylistTrackRepository extends JpaRepository<PlaylistTrack, Long> {

    // 특정 플레이리스트의 트랙들을 순서대로 조회
    List<PlaylistTrack> findByPlaylistOrderByTrackOrderAsc(Playlist playlist);

    // playlistId 기준으로 순서대로 조회
    List<PlaylistTrack> findByPlaylistIdOrderByTrackOrderAsc(Long playlistId);

    // 특정 플레이리스트의 트랙 목록 조회
    @Query("""
        select pt
        from PlaylistTrack pt
        join fetch pt.track t
        where pt.playlist.id = :playlistId
        order by pt.trackOrder asc
    """)
    List<PlaylistTrack> findAllWithTrackByPlaylistId(
            @Param("playlistId") Long playlistId
    );

    // 트랙 순서 밀기
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
        UPDATE playlist_track
        SET track_order = track_order + 1
        WHERE playlist_id = :playlistId
          AND track_order >= :trackOrder
        """, nativeQuery = true)
    int shiftOrdersForInsert(@Param("playlistId") Long playlistId,
                             @Param("trackOrder") int trackOrder);

    // 페이징을 위한 전체 목록 조회
    long countByPlaylistId(Long playlistId);


    // 특정 플레이리스트에서 특정 트랙만 삭제
    void deleteByPlaylistIdAndTrackId(Long playlistId, Long trackId);

    //  해당 플레이리스트에 이 트랙이 이미 존재하는지 체크
    boolean existsByPlaylistIdAndTrackId(Long playlistId, Long trackId);

    //  특정 플레이리스트에서 특정 트랙 1개 조회
    Optional<PlaylistTrack> findByPlaylistIdAndTrackId(Long playlistId, Long trackId);

    // 마지막 순서 조회
    @Query("select coalesce(max(pt.trackOrder), 0) from PlaylistTrack pt where pt.playlist.id = :playlistId")
    int findMaxOrder(@Param("playlistId") Long playlistId);

    @Query("""
    select pt
    from PlaylistTrack pt
    join fetch pt.track
    where pt.playlist.id = :playlistId
    order by pt.trackOrder asc
""")
    List<PlaylistTrack> findQueueByPlaylistId(@Param("playlistId") Long playlistId);

    // 리스트용 좋아요 집계 (ids IN (...) group by)
    @Query("""
        select pl.playlist.id as playlistId, count(pl.id) as cnt
        from PlaylistLike pl
        where pl.playlist.id in :playlistIds
        group by pl.playlist.id
    """)
    List<PlaylistLikeCountRow> countGroupByPlaylistIds(@Param("playlistIds") List<Long> playlistIds);
}
