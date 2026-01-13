package com.playlist.backend.integration.youtube.api.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class YoutubeVideosItemResponse {
    private String videoId;
    private String title;
    private String channelTitle;
    private String thumbnailUrl;
    private Integer durationSec; // 없으면 null
}
