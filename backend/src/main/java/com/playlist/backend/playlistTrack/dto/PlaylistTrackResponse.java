// src/main/java/com/playlist/backend/playlistTrack/dto/PlaylistTrackResponse.java
package com.playlist.backend.playlistTrack.dto;

import com.playlist.backend.playlistTrack.PlaylistTrack;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PlaylistTrackResponse {

    private Long id;
    private Long playlistId;
    private String source;       // YOUTUBE / SPOTIFY 등
    private String externalId;   // videoId / trackId
    private String title;
    private String artist;
    private String thumbnailUrl;
    private Long durationMs;
    private Integer trackOrder;

    public static PlaylistTrackResponse from(PlaylistTrack track) {
        return PlaylistTrackResponse.builder()
                .id(track.getId())
                .playlistId(track.getPlaylist().getId())
                .source(track.getSource())
                .externalId(track.getExternalId())
                .title(track.getTitle())
                .artist(track.getArtist())
                .thumbnailUrl(track.getThumbnailUrl())
                .durationMs(track.getDurationMs())
                .trackOrder(track.getTrackOrder())
                .build();
    }
}
