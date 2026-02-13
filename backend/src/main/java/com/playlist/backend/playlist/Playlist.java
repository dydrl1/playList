package com.playlist.backend.playlist;

import com.playlist.backend.playlistLike.PlaylistLike;
import com.playlist.backend.playlistTrack.PlaylistTrack;
import com.playlist.backend.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "playlist")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Playlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 소유자 (owner) 정보를 위해 명칭을 명확히 함
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private boolean isPublic = true;

    // 추가: 플레이리스트 대표 이미지 (프론트엔드 thumbnailUrl 대응)
    @Column(length = 500)
    private String thumbnailUrl;

    @Column(nullable = false)
    private long viewCount = 0L;


    // 양방향 매핑
    @OneToMany(mappedBy = "playlist", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlaylistTrack> playlistTracks = new ArrayList<>();

    @OneToMany(mappedBy = "playlist", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlaylistLike> likes = new ArrayList<>();

    @Builder
    public Playlist(User user, String title, String description, boolean isPublic, String thumbnailUrl) {
        this.user = user;
        this.title = title;
        this.description = description;
        this.isPublic = isPublic;
        this.thumbnailUrl = thumbnailUrl; // 생성 시 썸네일 추가 가능
        this.viewCount = 0L;
    }

    // =========================================
    //  비즈니스 로직 및 편의 메서드
    // =========================================

    // 조회수 증가 메서드
    public void incrementViewCount() {
        this.viewCount++;
    }

    // 썸네일 업데이트 (곡 추가 시 첫 번째 곡 이미지로 설정할 때 유용)
    public void updateThumbnail(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public void addTrack(PlaylistTrack playlistTrack) {
        playlistTracks.add(playlistTrack);
        playlistTrack.setPlaylist(this);

        // 썸네일이 비어있다면(첫 번째 곡이라면) 현재 추가되는 곡의 이미지로 업데이트
        if (this.thumbnailUrl == null || this.thumbnailUrl.isBlank()) {
            String trackImage = playlistTrack.getTrack().getImageUrl();
            this.updateThumbnail(trackImage); // 👈 작성하신 메서드 활용
        }
    }

    public void removeTrack(PlaylistTrack playlistTrack) {
        playlistTracks.remove(playlistTrack);
        playlistTrack.setPlaylist(null);
    }

    // == 수정 메서드 == //
    public void update(String title, String description, Boolean isPublic, String thumbnailUrl) {
        if (title != null) this.title = title;
        if (description != null) this.description = description;
        if (isPublic != null) this.isPublic = isPublic;
        if (thumbnailUrl != null) this.thumbnailUrl = thumbnailUrl;
    }

    /**
     * PlaylistResponse.from()에서 사용할 소유자 이름 Getter
     */
    public String getOwnerName() {
        return this.user != null ? this.user.getName() : "알 수 없음";
    }

}