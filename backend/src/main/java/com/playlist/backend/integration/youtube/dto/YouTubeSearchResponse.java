package com.playlist.backend.integration.youtube.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class YouTubeSearchResponse {

    private List<Item> items;

    @Getter
    @Setter
    public static class Item {
        private Id id;
        private Snippet snippet;
    }


    @Getter
    @Setter
    public static class Id {
        private String videoId;
    }

    @Getter
    @Setter
    public static class Snippet {
        private String title;
        private String channelTitle;
        private Thumbnails thumbnails;
    }


    @Getter
    @Setter
    public static class Thumbnails {
        private Thumb high;
        private Thumb medium;

        @com.fasterxml.jackson.annotation.JsonProperty("default")
        private Thumb defaultThumb; //
    }

    @Getter
    @Setter
    public static class Thumb {
        private String url;
    }
}
