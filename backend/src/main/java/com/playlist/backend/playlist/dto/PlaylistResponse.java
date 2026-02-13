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
        // 1. 기본적으로 저장된 썸네일을 가져오되
        String thumb = playlist.getThumbnailUrl();

        // 2. 만약 저장된 썸네일이 없다면 첫 번째 트랙의 이미지를 사용함
        if ((thumb == null || thumb.isEmpty())
                && playlist.getPlaylistTracks() != null
                && !playlist.getPlaylistTracks().isEmpty()) {

            // 첫 번째 트랙 엔티티를 꺼내서 그 이미지를 할당
            thumb = playlist.getPlaylistTracks().get(0).getTrack().getImageUrl();
        }

        return PlaylistResponse.builder()
                .id(playlist.getId())
                .title(playlist.getTitle())
                .description(playlist.getDescription())
                .ownerName(playlist.getOwnerName())
                .ownerId(playlist.getUser().getId())
                .thumbnailUrl(thumb) // 👈 위에서 가공한 thumb을 전달
                .trackCount(playlist.getPlaylistTracks().size())
                .viewCount((int)playlist.getViewCount())
                .isPublic(playlist.isPublic())
                .likeCount(likeCount)
                .isLiked(isLiked)
                .build();
    }
}
