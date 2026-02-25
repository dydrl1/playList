package com.playlist.backend.integration.youtube.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class YouTubeVideosResponse {

    private List<Item> items;

    @Getter
    @Setter
    public static class Item {
        private String id;
        private Snippet snippet;
        private ContentDetails contentDetails;
        private Status status;

        // [중요] 메서드를 Item 클래스 내부로 이동해야 status와 contentDetails에 접근 가능합니다.
        public boolean isPlayable() {
            // 1. 임베드 가능 여부 확인
            if (status != null && status.getEmbeddable() != null) {
                if (!status.getEmbeddable()) return false;
            }

            // 2. 지역 제한 확인
            if (contentDetails != null && contentDetails.getRegionRestriction() != null) {
                RegionRestriction rr = contentDetails.getRegionRestriction();

                // 차단 목록에 KR이 있으면 false
                if (rr.getBlocked() != null && rr.getBlocked().contains("KR")) {
                    return false;
                }

                // 허용 목록이 존재하는데 KR이 없으면 false
                if (rr.getAllowed() != null && !rr.getAllowed().isEmpty() && !rr.getAllowed().contains("KR")) {
                    return false;
                }
            }
            return true;
        }
    }

    @Getter
    @Setter
    public static class Status {
        private Boolean embeddable;
    }

    @Getter
    @Setter
    public static class Snippet {
        private String title;
        private String description;
        private String channelTitle;
        private Thumbnails thumbnails;
    }

    @Getter
    @Setter
    public static class ContentDetails {
        private String duration;
        private RegionRestriction regionRestriction; // 이 필드가 정의되어 있어야 지역 제한을 체크합니다.
    }

    @Getter
    @Setter
    public static class RegionRestriction {
        private List<String> allowed;
        private List<String> blocked;
    }

    @Getter
    @Setter
    public static class Thumbnails {
        private Thumb high;
        private Thumb medium;

        @JsonProperty("default")
        private Thumb defaultThumb;
    }

    @Getter
    @Setter
    public static class Thumb {
        private String url;
    }
}