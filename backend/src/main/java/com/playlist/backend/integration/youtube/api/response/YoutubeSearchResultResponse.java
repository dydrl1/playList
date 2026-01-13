package com.playlist.backend.integration.youtube.api.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class YoutubeSearchResultResponse {
    private List<YoutubeVideosItemResponse> items;
}
