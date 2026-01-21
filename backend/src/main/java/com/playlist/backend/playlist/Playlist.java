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

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private boolean isPublic = true;

    @Column(nullable = false)
    private long viewCount = 0L;

    // 양방향 매핑 (읽기 전용)
    @OneToMany(mappedBy = "playlist", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlaylistTrack> playlistTracks = new ArrayList<>();

    @OneToMany(mappedBy = "playlist", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlaylistLike> likes = new ArrayList<>();

    @Builder
    public Playlist(User user, String title, String description, boolean isPublic) {
        this.user = user;
        this.title = title;
        this.description = description;
        this.isPublic = isPublic;
    }

    // =========================================
    //  연관관계 편의 메서드
    // =========================================

    public void addTrack(PlaylistTrack playlistTrack) {
        playlistTracks.add(playlistTrack);
        playlistTrack.setPlaylist(this);
    }

    public void removeTrack(PlaylistTrack playlistTrack) {
        playlistTracks.remove(playlistTrack);
        playlistTrack.setPlaylist(null);
    }

    public void addLike(PlaylistLike like) {
        likes.add(like);
        like.setPlaylist(this);
    }

    public void removeLike(PlaylistLike like) {
        likes.remove(like);
        like.setPlaylist(null);
    }

    // =========================================
    // getter
    // =========================================

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public List<PlaylistTrack> getPlaylistTracks() {
        return playlistTracks;
    }

    public List<PlaylistLike> getLikes() {
        return likes;
    }

    // == 수정 메서드 == //
    public void update(String title, String description, Boolean isPublic) {
        if (title != null) {
            this.title = title;
        }
        if (description != null) {
            this.description = description;
        }
        if (isPublic != null) {
            this.isPublic = isPublic;
        }
    }
}
