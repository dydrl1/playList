package com.playlist.backend.playlist.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TrackAddRequest {
    private String title;
    private String artist;
    private String album;
    private String imageUrl;    // 👈 이 필드가 있어야 썸네일 업데이트가 가능합니다!
    private int durationSec;
    private String sourceType;  // 예: YOUTUBE
    private String sourceUrl;   // 예: 외부 ID (videoId)
}