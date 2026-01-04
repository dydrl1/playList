package com.playlist.backend.playlist;

import com.playlist.backend.playlistTrack.PlaylistTrack;
import org.springframework.data.jpa.repository.JpaRepository;
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


}
