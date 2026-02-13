// src/main/java/com/playlist/backend/playlistTrack/dto/PlaylistAddTracksRequest.java
package com.playlist.backend.playlistTrack.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class PlaylistTracksAddRequest {

    private List<TrackItem> tracks;

    @Getter
    @NoArgsConstructor
    public static class TrackItem {

        // 외부 트랙 기본 정보 (검색 결과에서 넘어옴)
        private String title;
        private String artist;
        private String album;
        private String imageUrl;
        private Integer durationSec;

        // 외부 소스 정보
        private String sourceType; // SPOTIFY / YOUTUBE
        private String sourceUrl;

        private Integer trackOrder; // null이면 마지막에 붙이기

        public String getImageUrl() { return imageUrl; }
    }
}
