// src/main/java/com/playlist/backend/playlistTrack/dto/PlaylistTrackResponse.java
package com.playlist.backend.playlistTrack.dto;

import com.playlist.backend.playlistTrack.PlaylistTrack;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PlaylistTrackResponse {

    private Long playlistTrackId;
    private Long playlistId;
    private Long trackId;
    private Integer trackOrder;

    private String title;
    private String artist;
    private String album;
    private String sourceType;
    private String sourceUrl;
    private String imageUrl;



    public static PlaylistTrackResponse from(PlaylistTrack playlistTrack) {
        var track = playlistTrack.getTrack();

        return PlaylistTrackResponse.builder()
                .playlistTrackId(playlistTrack.getId())
                .playlistId(playlistTrack.getPlaylist().getId())
                .trackId(track.getId())
                .trackOrder(playlistTrack.getTrackOrder())
                .title(track.getTitle())
                .artist(track.getArtist())
                .album(track.getAlbum())
                .imageUrl(track.getImageUrl())
                .sourceType(track.getSourceType())
                .sourceUrl(track.getSourceUrl())
                .build();
    }
}
