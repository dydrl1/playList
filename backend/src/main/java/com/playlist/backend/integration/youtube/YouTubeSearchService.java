package com.playlist.backend.integration.youtube;

import com.playlist.backend.integration.dto.ExternalTrackDto;
import com.playlist.backend.integration.dto.ProviderType;
import com.playlist.backend.integration.service.ExternalSearchService;
import com.playlist.backend.integration.youtube.dto.YouTubeSearchResponse;
import com.playlist.backend.integration.youtube.dto.YouTubeVideosResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class YouTubeSearchService implements ExternalSearchService {

    private static final String YOUTUBE_WATCH_BASE_URL = "https://www.youtube.com/watch?v=";

    private final YouTubeClient youTubeClient;

    @Override
    public ProviderType provider() {
        return ProviderType.YOUTUBE;
    }

    @Override
    public List<ExternalTrackDto> search(String query, int limit) {

        YouTubeSearchResponse searchRes = youTubeClient.search(query, limit);
        if (searchRes == null || searchRes.getItems() == null || searchRes.getItems().isEmpty()) {
            return List.of();
        }

        List<YouTubeSearchResponse.Item> searchItems = searchRes.getItems();

        List<String> videoIds = searchItems.stream()
                .map(it -> it.getId() != null ? it.getId().getVideoId() : null)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (videoIds.isEmpty()) return List.of();

        // videos.list는 실패해도 search 결과로 fallback 가능하게 처리
        Map<String, YouTubeVideosResponse.Item> byId = new HashMap<>();
        try {
            YouTubeVideosResponse videosRes = youTubeClient.videosByIds(String.join(",", videoIds));
            if (videosRes != null && videosRes.getItems() != null) {
                byId = videosRes.getItems().stream()
                        .filter(Objects::nonNull)
                        .filter(v -> v.getId() != null)
                        .collect(Collectors.toMap(YouTubeVideosResponse.Item::getId, v -> v, (a, b) -> a));
            }
        } catch (Exception ignored) {
            // fallback: search snippet만으로 응답
        }

        // 검색 순서 유지(검색 item 순서 그대로)
        List<ExternalTrackDto> result = new ArrayList<>(searchItems.size());

        for (YouTubeSearchResponse.Item sItem : searchItems) {
            if (sItem.getId() == null || sItem.getId().getVideoId() == null) continue;
            String id = sItem.getId().getVideoId();

            // 기본값: search snippet
            String title = sItem.getSnippet() != null && sItem.getSnippet().getTitle() != null
                    ? sItem.getSnippet().getTitle() : "";

            String channel = sItem.getSnippet() != null && sItem.getSnippet().getChannelTitle() != null
                    ? sItem.getSnippet().getChannelTitle() : "";

            String thumb = pickThumbFromSearch(sItem);

            Integer durationSec = null;

            // 보강값: videos snippet/contentDetails
            YouTubeVideosResponse.Item v = byId.get(id);
            if (v != null) {
                if (v.getSnippet() != null) {
                    if (v.getSnippet().getTitle() != null && !v.getSnippet().getTitle().isBlank()) {
                        title = v.getSnippet().getTitle();
                    }
                    if (v.getSnippet().getChannelTitle() != null && !v.getSnippet().getChannelTitle().isBlank()) {
                        channel = v.getSnippet().getChannelTitle();
                    }

                    String vThumb = pickThumbFromVideos(v.getSnippet());
                    if (vThumb != null) thumb = vThumb;
                }

                if (v.getContentDetails() != null) {
                    durationSec = parseIsoDurationToSec(v.getContentDetails().getDuration());
                }
            }

            result.add(ExternalTrackDto.builder()
                    .provider(ProviderType.YOUTUBE)
                    .externalId(id)
                    .title(title)
                    .artist(channel) // YouTube에서는 channelTitle을 artist로 매핑
                    .imageUrl(thumb)
                    .externalUrl(YOUTUBE_WATCH_BASE_URL + id)
                    .durationSec(durationSec)
                    .build());
        }

        return result;
    }

    // search.list 응답 썸네일 추출
    private static String pickThumbFromSearch(YouTubeSearchResponse.Item item) {
        if (item == null || item.getSnippet() == null || item.getSnippet().getThumbnails() == null) {
            return null;
        }
        YouTubeSearchResponse.Thumbnails t = item.getSnippet().getThumbnails();
        if (t.getHigh() != null) return t.getHigh().getUrl();
        if (t.getMedium() != null) return t.getMedium().getUrl();
        if (t.getDefaultThumb() != null) return t.getDefaultThumb().getUrl();
        return null;
    }

    // videos.list 응답 썸네일 추출 (누락되어 있던 메서드)
    private static String pickThumbFromVideos(YouTubeVideosResponse.Snippet snippet) {
        if (snippet == null || snippet.getThumbnails() == null) {
            return null;
        }
        YouTubeVideosResponse.Thumbnails t = snippet.getThumbnails();
        if (t.getHigh() != null) return t.getHigh().getUrl();
        if (t.getMedium() != null) return t.getMedium().getUrl();
        if (t.getDefaultThumb() != null) return t.getDefaultThumb().getUrl();
        return null;
    }

    // 예: "PT3M20S" -> 200
    private static Integer parseIsoDurationToSec(String iso) {
        if (iso == null || iso.isBlank()) return null;
        try {
            return (int) Duration.parse(iso).getSeconds();
        } catch (Exception e) {
            return null;
        }
    }
}