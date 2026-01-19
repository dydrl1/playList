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
import java.util.regex.Pattern;
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
        int fetchSize = Math.max(limit * 3, 20);

        // PASS 1: 엄격(정확도 우선)
        List<ScoredCandidate> pass1 = fetchAndScore(query, fetchSize, SearchMode.STRICT);

        // PASS 2: 부족하면 완화(결과 확보)
        List<ScoredCandidate> pass2 = List.of();
        if (pass1.size() < limit) {
            pass2 = fetchAndScore(query, fetchSize, SearchMode.RELAXED);
        }

        // 합치기(중복 제거) + 점수 정렬 + limit
        return mergeSortAndLimit(pass1, pass2, limit);
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

    
    // 검색 정확도를 높히기 위한 메서드 (통과/탈락 이 아닌 점수 반환)
    private static final Pattern NEG_TITLE = Pattern.compile(
            "(?i)\\b(cover|live|reaction|lyrics|dance|performance|teaser|mv|m/v|shorts|vlog)\\b|직캠|리액션|커버|가사|댄스"
    );

    private int scoreCandidate(String title, String channel, String desc, Integer durSec) {
        String t = safeLower(title);
        String c = safe(channel);
        String d = safeLower(desc);

        int score = 0;

        // 강한 긍정 신호
        if (c.endsWith(" - Topic")) score += 5;
        if (d.contains("provided to youtube")) score += 5;
        if (t.contains("official audio")) score += 3;

        // 보정(Topic 아닌 공식 채널 대응)
        if (t.contains("audio")) score += 2;
        if (t.contains("official")) score += 1;
        if (t.contains("music")) score += 1;

        // 강한 부정 신호
        if (NEG_TITLE.matcher(title == null ? "" : title).find()) score -= 6;

        // 길이 힌트
        if (durSec != null) {
            if (durSec >= 90 && durSec <= 420) score += 2;
            else if (durSec > 900) score -= 3;
        }

        return score;
    }

    private String safe(String s) { return s == null ? "" : s; }
    private String safeLower(String s) { return s == null ? "" : s.toLowerCase(); }




    private static class ScoredCandidate {
        final String videoId;
        final int score;
        final ExternalTrackDto dto;

        ScoredCandidate(String videoId, int score, ExternalTrackDto dto) {
            this.videoId = videoId;
            this.score = score;
            this.dto = dto;
        }
    }


    // fetchAndScore 검샘 -> videos.list 보강 -> 점수 계산
    private List<ScoredCandidate> fetchAndScore(String query, int fetchSize, SearchMode mode) {

        YouTubeSearchResponse searchRes = youTubeClient.search(query, fetchSize, mode); //  client에 mode 전달
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
            // videos.list 실패 시에도 search snippet만으로 점수 계산 가능
        }

        List<ScoredCandidate> scored = new ArrayList<>(searchItems.size());

        for (YouTubeSearchResponse.Item sItem : searchItems) {
            if (sItem.getId() == null || sItem.getId().getVideoId() == null) continue;

            String id = sItem.getId().getVideoId();

            // 기본값: search snippet
            String title = sItem.getSnippet() != null ? sItem.getSnippet().getTitle() : "";
            String channel = sItem.getSnippet() != null ? sItem.getSnippet().getChannelTitle() : "";
            String thumb = pickThumbFromSearch(sItem);

            // 보강값: videos snippet/contentDetails
            YouTubeVideosResponse.Item v = byId.get(id);

            String desc = "";
            Integer durationSec = null;

            if (v != null) {
                if (v.getSnippet() != null) {
                    if (v.getSnippet().getTitle() != null && !v.getSnippet().getTitle().isBlank()) title = v.getSnippet().getTitle();
                    if (v.getSnippet().getChannelTitle() != null && !v.getSnippet().getChannelTitle().isBlank()) channel = v.getSnippet().getChannelTitle();
                    if (v.getSnippet().getDescription() != null) desc = v.getSnippet().getDescription(); //  DTO에 description 있어야 함

                    String vThumb = pickThumbFromVideos(v.getSnippet());
                    if (vThumb != null) thumb = vThumb;
                }
                if (v.getContentDetails() != null) {
                    durationSec = parseIsoDurationToSec(v.getContentDetails().getDuration());
                }
            }

            int score = scoreCandidate(title, channel, desc, durationSec);

            // 최소 컷(운영하며 조정): STRICT는 조금 높게, RELAXED는 낮게
            int cutoff = (mode == SearchMode.STRICT) ? 1 : 0;
            if (score < cutoff) continue;

            ExternalTrackDto dto = ExternalTrackDto.builder()
                    .provider(ProviderType.YOUTUBE)
                    .externalId(id)
                    .title(title)
                    .artist(channel)
                    .imageUrl(thumb)
                    .externalUrl(YOUTUBE_WATCH_BASE_URL + id)
                    .durationSec(durationSec)
                    .build();

            scored.add(new ScoredCandidate(id, score, dto));
        }

        // 점수 높은 순으로 정렬
        scored.sort((a, b) -> Integer.compare(b.score, a.score));
        return scored;
    }


    private List<ExternalTrackDto> mergeSortAndLimit(
            List<ScoredCandidate> pass1,
            List<ScoredCandidate> pass2,
            int limit
    ) {
        // videoId 기준 중복 제거 + 더 높은 점수 유지
        Map<String, ScoredCandidate> best = new HashMap<>();

        for (ScoredCandidate c : pass1) {
            best.put(c.videoId, c);
        }
        for (ScoredCandidate c : pass2) {
            best.merge(c.videoId, c, (a, b) -> (b.score > a.score) ? b : a);
        }

        return best.values().stream()
                .sorted((a, b) -> Integer.compare(b.score, a.score))
                .limit(limit)
                .map(c -> c.dto)
                .toList();
    }


}