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
    private String ownerName;    //  화면 표시용
    private Long ownerId;       //   내 플리 여부 확인용
    private String thumbnailUrl; //  썸네일
    private int trackCount;     //  곡 개수
    private int viewCount;      //  조회수
    private int likeCount;
    private boolean isLiked;    // 현재 사용자의 좋아요 여부
    private boolean isPublic;


    public static PlaylistResponse from(Playlist playlist, int likeCount, boolean isLiked) {
        return PlaylistResponse.builder()
                .id(playlist.getId())
                .title(playlist.getTitle())
                .description(playlist.getDescription())
                .ownerName(playlist.getOwnerName()) // 연관관계에 따라 수정
                .ownerId(playlist.getUser().getId())
                .thumbnailUrl(playlist.getThumbnailUrl())
                .trackCount(playlist.getPlaylistTracks().size()) // 트랙 개수
                .viewCount((int)playlist.getViewCount())
                .isPublic(playlist.isPublic())
                .likeCount(likeCount)
                .isLiked(isLiked) // 이 부분이 들어가야 하트가 색칠됩니다.
                .build();
    }
}
