package com.playlist.backend.playlistTrack;

import com.playlist.backend.Track.Track;
import com.playlist.backend.playlist.Playlist;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "playlist_track",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_playlist_track",
                        columnNames = {"playlist_id", "track_id"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlaylistTrack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 플레이리스트인지
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "playlist_id", nullable = false)
    private Playlist playlist;

    // 어떤 곡인지
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "track_id", nullable = false)
    private Track track;

    // 플레이리스트 내 순서
    @Column(nullable = false)
    private Integer trackOrder;

    @Column(nullable = false, updatable = false)
    private LocalDateTime addedAt;

    //  Builder 생성자 (Service에서 사용)
    @Builder
    public PlaylistTrack(Playlist playlist, Track track, Integer trackOrder) {
        this.playlist = playlist;
        this.track = track;
        this.trackOrder = trackOrder;
        this.addedAt = LocalDateTime.now();
    }

    public void setTrackOrder(Integer trackOrder) {
        this.trackOrder = trackOrder;
    }

    public void setPlaylist(Playlist playlist) {
        this.playlist = playlist;
    }

    // === 연관관계 편의 ===

    public void changeOrder(Integer trackOrder) {
        this.trackOrder = trackOrder;
    }

    public static PlaylistTrack create(Playlist playlist, Track track, int order) {
        PlaylistTrack pt = new PlaylistTrack();
        pt.playlist = playlist;
        pt.track = track;
        pt.trackOrder = order;
        pt.addedAt = LocalDateTime.now();
        return pt;
    }
}
