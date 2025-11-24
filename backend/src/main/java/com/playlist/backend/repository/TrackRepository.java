package com.playlist.backend.repository;

import com.playlist.backend.entity.Track;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrackRepository extends JpaRepository<Track, Long> {

    // 제목 키워드 검색
    List<Track> findByTitleContainingIgnoreCase(String keyword);

    // 아티스트 이름으로 검색
    List<Track> findByArtistContainingIgnoreCase(String artist);
}
