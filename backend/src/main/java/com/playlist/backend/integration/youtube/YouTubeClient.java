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

    //  기존 메서드: 기본 모드(STRICT)로 위임
    public YouTubeSearchResponse search(String query, int limit) {
        return search(query, limit, SearchMode.STRICT);
    }

    //  신규 오버로드 메서드: mode에 따라 파라미터/쿼리 보정
    public YouTubeSearchResponse search(String query, int limit, SearchMode mode) {
        String q = buildQuery(query, mode);

        return youtubeWebClient.get()
                .uri(uriBuilder -> {
                    var b = uriBuilder
                            .path("/search")
                            .queryParam("part", "snippet")
                            .queryParam("type", "video")
                            .queryParam("maxResults", limit)
                            .queryParam("q", q)
                            .queryParam("key", apiKey)
                            // 재생 목적이면 추천(임베드 가능 영상 위주)
                            .queryParam("videoEmbeddable", "true");

                    // STRICT에서만 음악으로 강하게 좁힘
                    if (mode == SearchMode.STRICT) {
                        b = b.queryParam("videoCategoryId", "10") // Music
                                .queryParam("topicId", "/m/04rlf");  // Music topic
                    }

                    return b.build();
                })
                .retrieve()
                .bodyToMono(YouTubeSearchResponse.class)
                .block();
    }

    public YouTubeVideosResponse videosByIds(String commaSeparatedIds) {
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

    // mode별 쿼리 보정 (서비스 정책에 맞게 조절)
    private String buildQuery(String query, SearchMode mode) {
        if (mode == SearchMode.STRICT) {
            return query + " official audio -cover -live -reaction -lyrics -mv -m/v -shorts -vlog";
        }
        // RELAXED: 결과 확보용(너무 공격적으로 빼지 않기)
        return query + " -reaction -vlog";
    }
}
