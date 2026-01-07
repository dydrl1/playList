package com.playlist.backend.integration.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExternalTrackDto {

    private ProviderType provider;    // YOUTUBE
    private String providerTrackId;   // videoId
    private String title;             // 영상 제목
    private String artist;            // 아티스트 (channelTitle)
    private String imageUrl;          // 썸네일
    private String externalUrl;       // watch URL
    private Integer durationSec;      // 재생 시간(초)
}
