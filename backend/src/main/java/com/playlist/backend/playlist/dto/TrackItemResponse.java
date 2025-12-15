package com.playlist.backend.playlist.dto;

import com.playlist.backend.playlistTrack.PlaylistTrack;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TrackItemResponse {

    private Long playlistTrackId;
    private Long trackId;
    private Integer trackOrder;
    private String title;
    private String artist;
    private String album;
    private Integer durationSec;
    private String sourceType;
    private String sourceUrl;

    public static TrackItemResponse from(PlaylistTrack pt) {
        return TrackItemResponse.builder()
                .playlistTrackId(pt.getId())
                .trackId(pt.getTrack().getId())
                .trackOrder(pt.getTrackOrder())
                .title(pt.getTrack().getTitle())
                .artist(pt.getTrack().getArtist())
                .album(pt.getTrack().getAlbum())
                .durationSec(pt.getTrack().getDurationSec())
                .sourceType(pt.getTrack().getSourceType())
                .sourceUrl(pt.getTrack().getSourceUrl())
                .build();
    }
}
