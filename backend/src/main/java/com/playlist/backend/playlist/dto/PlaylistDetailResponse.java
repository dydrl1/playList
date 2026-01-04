package com.playlist.backend.playlist.dto;

import com.playlist.backend.playlist.Playlist;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PlaylistDetailResponse {
    private Long playlistId;
    private Long ownerUserId;
    private String title;
    private String description;
    private boolean isPublic;

    private int likeCount;
    private boolean likedByMe; // 로그인 기능 붙어있으면 사용

    private List<TrackItemResponse> tracks;

    public static PlaylistDetailResponse of(
            Playlist p,
            int likeCount,
            boolean likedByMe,
            List<TrackItemResponse> tracks
    ) {
        return PlaylistDetailResponse.builder()
                .playlistId(p.getId())
                .ownerUserId(p.getUser().getId())
                .title(p.getTitle())
                .description(p.getDescription())
                .isPublic(p.isPublic())
                .likeCount(likeCount)
                .likedByMe(likedByMe)
                .tracks(tracks)
                .build();
    }
}
