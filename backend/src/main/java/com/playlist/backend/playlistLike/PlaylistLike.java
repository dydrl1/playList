package com.playlist.backend.playlistLike;

import com.playlist.backend.playlist.Playlist;
import com.playlist.backend.user.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "PLAYLIST_LIKE",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_playlist_like",
                        columnNames = {"user_id", "playlist_id"}
                )
        }
)
// 유저가 어떤 플레이리스트에 좋아요 눌렀는지
public class PlaylistLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;   // PK

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;        // 좋아요 누른 유저


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "playlist_id")
    private Playlist playlist;  // 좋아요 대상 플레이리스트


    private LocalDateTime createdAt;



    protected PlaylistLike() {
    }

    public PlaylistLike(User user, Playlist playlist) {
        this.user = user;
        this.playlist = playlist;
        this.createdAt = LocalDateTime.now();
    }

    // === getter / setter ===

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Playlist getPlaylist() {
        return playlist;
    }

    public void setPlaylist(Playlist playlist) {
        this.playlist = playlist;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
