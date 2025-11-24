package com.playlist.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "TRACK") // 곡/트랙 정보
public class Track {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;   // PK

    @Column(nullable = false, length = 200)
    private String title;   // 곡 제목

    @Column(nullable = false, length = 100)
    private String artist;  // 아티스트

    @Column(length = 100)
    private String album;   // 앨범명 (선택)

    // 초 단위 재생 시간 (선택)
    private Integer durationSec;

    // YOUTUBE / FILE / OTHER 등
    @Column(length = 20)
    private String sourceType;

    // 유튜브 링크 등
    @Column(length = 255)
    private String sourceUrl;

    private LocalDateTime createdAt;

    protected Track() {
    }

    public Track(String title, String artist) {
        this.title = title;
        this.artist = artist;
        this.createdAt = LocalDateTime.now();
    }

    // === getter / setter ===

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public Integer getDurationSec() {
        return durationSec;
    }

    public void setDurationSec(Integer durationSec) {
        this.durationSec = durationSec;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
