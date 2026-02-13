package com.playlist.backend.Track.dto;

import lombok.Getter;

@Getter
public class TrackUpdateRequest {

    private String title;
    private String artist;
    private String album;
    private Integer durationSec;
    private String sourceType;
    private String sourceUrl;
    private String imageUrl;

}
