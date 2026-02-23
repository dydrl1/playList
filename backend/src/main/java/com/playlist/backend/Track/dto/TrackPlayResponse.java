package com.playlist.backend.Track.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TrackPlayResponse {
    private Long trackId;
    private String title;
    private String artist;
    private String sourceType;  // 예: YOUTUBE
    private String sourceUrl;   // 유튜브 영상 ID (externalId)
    private String imageUrl;
}
