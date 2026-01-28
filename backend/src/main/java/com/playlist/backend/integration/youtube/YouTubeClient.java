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

    //  기존 메서드: 기본 모드(STRICT)로 위임
    public YouTubeSearchResponse search(String query, int limit) {
        return search(query, limit, SearchMode.STRICT);
    }

    //  신규 오버로드 메서드: mode에 따라 파라미터/쿼리 보정
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
                                .queryParam("videoEmbeddable", "true");

                        if (mode == SearchMode.STRICT) {
                            b = b.queryParam("videoCategoryId", "10")
                                    .queryParam("topicId", "/m/04rlf");
                        }
                        return b.build();
                    })
                    .retrieve()
                    .bodyToMono(YouTubeSearchResponse.class)
                    .block();

            // ✅ body가 null이면 외부 응답 이상
            if (body == null) {
                throw new BusinessException(ErrorCode.EXTERNAL_API_RESPONSE_INVALID);
            }

            return body;

        } catch (WebClientRequestException e) {
            //  네트워크/연결/타임아웃 계열
            throw new BusinessException(ErrorCode.EXTERNAL_API_TIMEOUT);

        } catch (WebClientResponseException e) {
            //  외부 서비스가 4xx/5xx 응답을 준 경우(쿼터 429는 별도 처리 가능)
            if (e.getStatusCode().value() == 429) {
                throw new BusinessException(ErrorCode.YOUTUBE_QUOTA_EXCEEDED);
            }
            throw new BusinessException(ErrorCode.EXTERNAL_API_RESPONSE_INVALID);

        } catch (Exception e) {
            //  JSON 파싱 실패 등 예상 못한 변환 오류도 외부 응답 이상으로 통일
            throw new BusinessException(ErrorCode.EXTERNAL_API_RESPONSE_INVALID);
        }
    }


    public YouTubeVideosResponse videosByIds(String commaSeparatedIds) {
        try {
            YouTubeVideosResponse body = youtubeWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/videos")
                            .queryParam("part", "contentDetails,snippet")
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

        } catch (WebClientRequestException e) {
            throw new BusinessException(ErrorCode.EXTERNAL_API_TIMEOUT);

        } catch (WebClientResponseException e) {
            if (e.getStatusCode().value() == 429) {
                throw new BusinessException(ErrorCode.YOUTUBE_QUOTA_EXCEEDED);
            }
            throw new BusinessException(ErrorCode.EXTERNAL_API_RESPONSE_INVALID);

        } catch (Exception e) {
            throw new BusinessException(ErrorCode.EXTERNAL_API_RESPONSE_INVALID);
        }
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
