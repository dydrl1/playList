package com.playlist.backend.integration.youtube;

import com.playlist.backend.common.exception.BusinessException;
import com.playlist.backend.common.exception.ErrorCode;
import com.playlist.backend.integration.youtube.dto.YouTubeSearchResponse;
import com.playlist.backend.integration.youtube.dto.YouTubeVideosResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
@RequiredArgsConstructor
public class YouTubeClient {

    private final WebClient youtubeWebClient;

    @Value("${youtube.api-key}")
    private String apiKey;

    public YouTubeSearchResponse search(String query, int limit) {
        return search(query, limit, SearchMode.STRICT);
    }

    public YouTubeSearchResponse search(String query, int limit, SearchMode mode) {
        String q = buildQuery(query, mode);

        try {
            YouTubeSearchResponse body = youtubeWebClient.get()
                    .uri(uriBuilder -> {
                        var b = uriBuilder
                                .path("/search")
                                .queryParam("part", "snippet")
                                .queryParam("type", "video")
                                .queryParam("maxResults", limit)
                                .queryParam("q", q)
                                .queryParam("key", apiKey)
                                .queryParam("videoEmbeddable", "true"); // 1차 필터링

                        if (mode == SearchMode.STRICT) {
                            b = b.queryParam("videoCategoryId", "10")
                                    .queryParam("topicId", "/m/04rlf");
                        }
                        return b.build();
                    })
                    .retrieve()
                    .bodyToMono(YouTubeSearchResponse.class)
                    .block();

            if (body == null) throw new BusinessException(ErrorCode.EXTERNAL_API_RESPONSE_INVALID);
            return body;
        } catch (Exception e) {
            handleException(e);
            return null;
        }
    }

    /**
     *  videosByIds 메서드
     * part 파라미터에 'status'를 추가하여 임베드 가능 여부를 받아옵니다.
     */
    public YouTubeVideosResponse videosByIds(String commaSeparatedIds) {
        try {
            YouTubeVideosResponse body = youtubeWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/videos")
                            // ✅ status를 추가해야 DTO의 isPlayable()이 동작합니다.
                            .queryParam("part", "contentDetails,snippet,status")
                            .queryParam("id", commaSeparatedIds)
                            .queryParam("key", apiKey)
                            .build())
                    .retrieve()
                    .bodyToMono(YouTubeVideosResponse.class)
                    .block();

            if (body == null) {
                throw new BusinessException(ErrorCode.EXTERNAL_API_RESPONSE_INVALID);
            }
            return body;

        } catch (Exception e) {
            handleException(e);
            return null;
        }
    }

    // 예외 처리 로직 공통화 (코드 깔끔하게 정리)
    private void handleException(Exception e) {
        if (e instanceof WebClientRequestException) {
            throw new BusinessException(ErrorCode.EXTERNAL_API_TIMEOUT);
        } else if (e instanceof WebClientResponseException resEx) {
            if (resEx.getStatusCode().value() == 429) {
                throw new BusinessException(ErrorCode.YOUTUBE_QUOTA_EXCEEDED);
            }
        }
        throw new BusinessException(ErrorCode.EXTERNAL_API_RESPONSE_INVALID);
    }

    private String buildQuery(String query, SearchMode mode) {
        if (mode == SearchMode.STRICT) {
            // 검색 시 부정어 키워드를 -로 붙여주는 방식은 매우 효과적입니다.
            return query + " official audio -cover -live -reaction -lyrics -shorts -vlog -karaoke";
        }
        return query + " -reaction -vlog";
    }
}
