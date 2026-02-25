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
    private String ownerName;
    private String title;
    private String description;
    private boolean isPublic;

    private int likeCount;
    private boolean likedByMe; // 로그인 기능 붙어있으면 사용
    private boolean isOwner;   // 주인 여부

    private List<TrackItemResponse> tracks;

    public static PlaylistDetailResponse of(
            Playlist p,
            long latestViewCount, int likeCount,
            boolean likedByMe,
            List<TrackItemResponse> tracks,
            Long currentUserId    // 현재 로그인한 유저 ID를 파라미터로 받음
    ) {
        // 현재 유저 ID와 플레이리스트 소유자 ID를 비교하여 isOwner 결정
        boolean isOwner = currentUserId != null && p.getUser().getId().equals(currentUserId);

        return PlaylistDetailResponse.builder()
                .playlistId(p.getId())
                .ownerUserId(p.getUser().getId())
                .ownerName(p.getUser().getName())
                .title(p.getTitle())
                .description(p.getDescription())
                .isPublic(p.isPublic())
                .likeCount(likeCount)
                .likedByMe(likedByMe)
                .isOwner(isOwner)
                .tracks(tracks)
                .build();
    }
}
