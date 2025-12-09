package com.playlist.backend.playlistTrack;

import com.playlist.backend.playlist.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlaylistTrackRepository extends JpaRepository<PlaylistTrack, Long> {

    // 특정 플레이리스트의 트랙들을 순서대로 조회
    List<PlaylistTrack> findByPlaylistOrderByTrackOrderAsc(Playlist playlist);

    // playlistId 기준으로 순서대로 조회
    List<PlaylistTrack> findByPlaylistIdOrderByTrackOrderAsc(Long playlistId);

    // 특정 플레이리스트에서 특정 트랙만 삭제
    void deleteByPlaylistIdAndTrackId(Long playlistId, Long trackId);

    // ✅ 해당 플레이리스트에 이 트랙이 이미 존재하는지 체크
    boolean existsByPlaylistIdAndTrackId(Long playlistId, Long trackId);

    // ✅ 특정 플레이리스트에서 특정 트랙 1개 조회
    Optional<PlaylistTrack> findByPlaylistIdAndTrackId(Long playlistId, Long trackId);
}
