// src/main/java/com/playlist/backend/playlistTrack/dto/PlaylistAddTracksRequest.java
package com.playlist.backend.playlistTrack.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class PlaylistAddTracksRequest {

    private List<TrackItem> tracks;

    @Getter
    @NoArgsConstructor
    public static class TrackItem {
        private String source;       // "YOUTUBE" / "SPOTIFY"
        private String externalId;   // YouTube videoId / Spotify trackId
        private String title;
        private String artist;
        private String thumbnailUrl;
        private Long durationMs;
        private Integer trackOrder;  // 재생 순서 (옵션)
    }
}
