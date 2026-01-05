package com.playlist.backend.playlist.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class PlaybackQueueResponse {

    private final Long playlistId;
    private final List<QueueItem> items;

    private PlaybackQueueResponse(Long playlistId, List<QueueItem> items){
        this.playlistId = playlistId;
        this.items = items;
    }

    public static PlaybackQueueResponse of(Long playlistId, List<QueueItem> items){
        return new PlaybackQueueResponse(playlistId, items);
    }

    @Getter
    public static class QueueItem {
        private final Long playlistTrackId;
        private final Integer order;

        private final Long trackId;
        private final String title;
        private final String artist;
        private final String album;
        private final Integer durationSec;

        private final String sourceType;
        private final String sourceUrl;

        private QueueItem(
                Long playlistTrackId,
                Integer order,
                Long trackId,
                String title,
                String artist,
                String album,
                Integer durationSec,
                String sourceType,
                String sourceUrl
        ){
            this.playlistTrackId = playlistTrackId;
            this.order = order;
            this.trackId = trackId;
            this.title = title;
            this.artist = artist;
            this.album = album;
            this.durationSec = durationSec;
            this.sourceType = sourceType;
            this.sourceUrl = sourceUrl;
        }

        public static QueueItem from(com.playlist.backend.playlistTrack.PlaylistTrack pt) {
            com.playlist.backend.Track.Track t = pt.getTrack();

            return new QueueItem(
                    pt.getId(),
                    pt.getTrackOrder(),
                    t.getId(),
                    t.getTitle(),
                    t.getArtist(),
                    t.getAlbum(),
                    t.getDurationSec(),
                    t.getSourceType(),
                    t.getSourceUrl()
            );
        }
    }

}
