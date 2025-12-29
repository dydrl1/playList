package com.playlist.backend.playlist.dto;

import com.playlist.backend.playlist.Playlist;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PlaylistResponse {

    private Long id;
    private String title;
    private String description;
    private boolean isPublic;

    public static PlaylistResponse from(Playlist playlist){
        return PlaylistResponse.builder()
                .id(playlist.getId())
                .title(playlist.getTitle())
                .description(playlist.getDescription())
                .isPublic(playlist.isPublic())
                .build();
    }
}
