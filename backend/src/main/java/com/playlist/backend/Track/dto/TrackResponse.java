package com.playlist.backend.Track.dto;

import com.playlist.backend.Track.Track;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class TrackResponse {
    private Long id;
    private String title;
    private String artist;
    private String album;
    private Integer durationSec;
    private String sourceType;
    private String sourceUrl;
    private LocalDateTime createdAt;

    public static TrackResponse from(Track track){
        return TrackResponse.builder()
                .id(track.getId())
                .title(track.getTitle())
                .artist(track.getArtist())
                .album(track.getAlbum())
                .durationSec(track.getDurationSec())
                .sourceType(track.getSourceType())
                .sourceUrl(track.getSourceUrl())
                .createdAt(track.getCreatedAt())
                .build();
    }
}
