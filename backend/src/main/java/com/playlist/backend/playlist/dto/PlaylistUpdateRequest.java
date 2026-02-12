package com.playlist.backend.playlist.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PlaylistUpdateRequest {

    private String title;
    private String description;
    private Boolean isPublic;
    private String thumbnailUrl;
}
