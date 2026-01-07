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

        List<String> videoIds = searchRes.getItems().stream()
                .map(it -> it.getId() != null ? it.getId().getVideoId() : null)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (videoIds.isEmpty()) return List.of();

        YouTubeVideosResponse videosRes = youTubeClient.videosByIds(String.join(",", videoIds));
        if (videosRes == null || videosRes.getItems() == null || videosRes.getItems().isEmpty()) {
            return List.of();
        }

        // id -> item 맵
        Map<String, YouTubeVideosResponse.Item> byId = videosRes.getItems().stream()
                .filter(Objects::nonNull)
                .filter(v -> v.getId() != null)
                .collect(Collectors.toMap(YouTubeVideosResponse.Item::getId, v -> v, (a, b) -> a));

        // 검색 순서 유지
        List<ExternalTrackDto> result = new ArrayList<>();
        for (String id : videoIds) {
            YouTubeVideosResponse.Item v = byId.get(id);
            if (v == null || v.getSnippet() == null) continue;

            result.add(ExternalTrackDto.builder()
                    .provider(ProviderType.YOUTUBE)
                    .providerTrackId(id)
                    .title(v.getSnippet().getTitle())
                    .artist(v.getSnippet().getChannelTitle())
                    .imageUrl(pickThumb(v.getSnippet()))
                    .externalUrl("https://www.youtube.com/watch?v=" + id)
                    .durationSec(parseIsoDurationToSec(v.getContentDetails() != null ? v.getContentDetails().getDuration() : null))
                    .build());
        }

        return result;
    }

    private static String pickThumb(YouTubeVideosResponse.Snippet snippet) {
        if (snippet.getThumbnails() == null) return null;
        YouTubeVideosResponse.Thumbnails t = snippet.getThumbnails();
        if (t.getHigh() != null) return t.getHigh().getUrl();
        if (t.getMedium() != null) return t.getMedium().getUrl();
        if (t.getDefaultThumb() != null) return t.getDefaultThumb().getUrl();
        return null;
    }

    // 예: "PT3M20S" -> 200
    private static Integer parseIsoDurationToSec(String iso) {
        if (iso == null || iso.isBlank()) return 0;
        try {
            return (int) Duration.parse(iso).getSeconds();
        } catch (Exception e) {
            return 0;
        }
    }
}
