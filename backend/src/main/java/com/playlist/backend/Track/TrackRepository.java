package com.playlist.backend.Track;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrackRepository extends JpaRepository<Track, Long> {

    // 간단 검색(선택): 제목/아티스트에 키워드 포함
    List<Track> findByTitleContainingIgnoreCaseOrArtistContainingIgnoreCase(String title, String artist);
}
