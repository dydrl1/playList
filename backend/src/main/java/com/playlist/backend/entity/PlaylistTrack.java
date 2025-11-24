package com.playlist.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "PLAYLIST_TRACK",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_playlist_track",
                        columnNames = {"playlist_id", "track_id"}
                )
        }
)
// 플레이리스트 안의 곡들 + 순서
public class PlaylistTrack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;   // PK

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "playlist_id")
    private Playlist playlist;   // 어떤 플레이리스트인지

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "track_id")
    private Track track;         // 어떤 곡인지

    @Column(nullable = false)
    private Integer trackOrder;  // 플레이리스트 내 순서 (1,2,3...)

    private LocalDateTime addedAt;

    protected PlaylistTrack() {
    }

    public PlaylistTrack(Playlist playlist, Track track, Integer trackOrder) {
        this.playlist = playlist;
        this.track = track;
        this.trackOrder = trackOrder;
        this.addedAt = LocalDateTime.now();
    }

    // === getter / setter ===

    public Long getId() {
        return id;
    }

    public Playlist getPlaylist() {
        return playlist;
    }

    public void setPlaylist(Playlist playlist) {
        this.playlist = playlist;
    }

    public Track getTrack() {
        return track;
    }

    public void setTrack(Track track) {
        this.track = track;
    }

    public Integer getTrackOrder() {
        return trackOrder;
    }

    public void setTrackOrder(Integer trackOrder) {
        this.trackOrder = trackOrder;
    }

    public LocalDateTime getAddedAt() {
        return addedAt;
    }
}
