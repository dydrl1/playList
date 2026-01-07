package com.playlist.backend.integration.youtube;


import com.playlist.backend.integration.youtube.dto.YouTubeSearchResponse;
import com.playlist.backend.integration.youtube.dto.YouTubeVideosResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class YouTubeClient {

    private final WebClient youtubeWebClient;

    @Value("${youtube.api-key}")
    private String apiKey;

    public YouTubeSearchResponse search(String query, int limit){
        return youtubeWebClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/search")
                    .queryParam("part", "snippet")
                    .queryParam("type", "video")
                    .queryParam("maxResults", limit)
                    .queryParam("q", query)
                    .queryParam("key", apiKey)
                    .build())
                .retrieve()
                .bodyToMono(YouTubeSearchResponse.class)
                .block();

    }

    public YouTubeVideosResponse videosByIds(String commaSeparatedIds){
        return youtubeWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/videos")
                        .queryParam("part", "contentDetails,snippet")
                        .queryParam("id", commaSeparatedIds)
                        .queryParam("key", apiKey)
                        .build())
                .retrieve()
                .bodyToMono(YouTubeVideosResponse.class)
                .block();
    }
}
