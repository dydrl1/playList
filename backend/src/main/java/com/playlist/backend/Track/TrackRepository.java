package com.playlist.backend.Track;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TrackRepository extends JpaRepository<Track, Long> {

    // 이미 담긴 트랙인지
    Optional<Track> findBySourceTypeAndSourceUrl(String sourceType, String sourceUrl);

    // 간단 검색(선택): 제목/아티스트에 키워드 포함
    List<Track> findByTitleContainingIgnoreCaseOrArtistContainingIgnoreCase(String title, String artist);

    Optional<Track> findBySourceTypeAndSourceId(String sourceType, String sourceId);
}
