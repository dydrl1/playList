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
        private String duration; // ISO 8601 e.g. PT3M20S
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
