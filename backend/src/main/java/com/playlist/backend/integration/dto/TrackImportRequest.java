package com.playlist.backend.integration.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class TrackImportRequest {

    @NotBlank
    private String sourceId;      // 유튜브 videoId

    @NotBlank
    private String sourceType;   // "YOUTUBE"

    @NotBlank
    private String title;

    @NotBlank
    private String artist;      // 지금은 channelTitle 매핑

    private Integer durationSec;
    private String sourceUrl;
    private String album;
    private String imageUrl;

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

}
