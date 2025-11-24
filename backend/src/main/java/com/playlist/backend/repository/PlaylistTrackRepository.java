package com.playlist.backend.repository;

import com.playlist.backend.entity.Playlist;
import com.playlist.backend.entity.PlaylistTrack;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlaylistTrackRepository extends JpaRepository<PlaylistTrack, Long> {

    // 특정 플레이리스트의 트랙들을 순서대로 조회
    List<PlaylistTrack> findByPlaylistOrderByTrackOrderAsc(Playlist playlist);

    // playlistId 기준으로 순서대로 조회
    List<PlaylistTrack> findByPlaylistIdOrderByTrackOrderAsc(Long playlistId);

    // 특정 플레이리스트에서 특정 트랙만 삭제
    void deleteByPlaylistIdAndTrackId(Long playlistId, Long trackId);
}
