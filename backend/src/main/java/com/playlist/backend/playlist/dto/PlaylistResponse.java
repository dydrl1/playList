package com.playlist.backend.playlist.dto;

import com.playlist.backend.playlist.Playlist;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PlaylistResponse {

    private Long id;
    private String title;
    private String description;
    private String ownerName;
    private Long ownerId;
    private String thumbnailUrl;
    private int trackCount;
    private long viewCount;
    private int likeCount;
    private boolean isLiked;
    private boolean isPublic;

    // [수정] 파라미터에 long viewCount 추가
    public static PlaylistResponse from(Playlist playlist, long viewCount, int likeCount, boolean isLiked) {

        String thumb = playlist.getThumbnailUrl();

        if ((thumb == null || thumb.isEmpty())
                && playlist.getPlaylistTracks() != null
                && !playlist.getPlaylistTracks().isEmpty()) {
            thumb = playlist.getPlaylistTracks().get(0).getTrack().getImageUrl();
        }

        return PlaylistResponse.builder()
                .id(playlist.getId())
                .title(playlist.getTitle())
                .description(playlist.getDescription())
                .ownerName(playlist.getOwnerName())
                .ownerId(playlist.getUser().getId())
                .thumbnailUrl(thumb)
                .trackCount(playlist.getPlaylistTracks().size())
                // [수정] playlist.getViewCount() 대신 파라미터로 받은 viewCount 사용
                .viewCount(viewCount)
                .isPublic(playlist.isPublic())
                .likeCount(likeCount)
                .isLiked(isLiked)
                .build();
    }
}
