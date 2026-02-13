package com.playlist.backend.integration.youtube;

import com.playlist.backend.Track.Track;
import com.playlist.backend.Track.TrackRepository;
import com.playlist.backend.integration.dto.TrackImportRequest;
import com.playlist.backend.playlist.Playlist;
import com.playlist.backend.playlist.PlaylistRepository;
import com.playlist.backend.playlistTrack.PlaylistTrack;
import com.playlist.backend.playlistTrack.PlaylistTrackRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExternalTrackImportService {

    private final TrackRepository trackRepository;
    private final PlaylistRepository playlistRepository;
    private final PlaylistTrackRepository playlistTrackRepository;

    @Transactional
    public PlaylistTrack importToPlaylist(Long playlistId, Long userId, TrackImportRequest req) {

        // 1) Playlist 조회 + 소유권 체크(본인만 담기 가능하게)
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new IllegalArgumentException("playlist not found"));

        if (!playlist.getUser().getId().equals(userId)) {
            throw new IllegalStateException("no permission");
        }

        // 2) Track upsert (sourceType + sourceId)
        Track track = trackRepository.findBySourceTypeAndSourceId(req.getSourceType(), req.getSourceId())
                .orElseGet(() -> {
                    Track t = new Track(req.getTitle(), req.getArtist(), req.getImageUrl());
                    t.setSourceType(req.getSourceType());
                    t.setSourceId(req.getSourceId());
                    t.setSourceUrl(req.getSourceUrl());
                    t.setAlbum(req.getAlbum());
                    t.setDurationSec(req.getDurationSec());
                    t.setImageUrl(req.getImageUrl());
                    return t;
                });

        // 이미 존재하는 Track이면 메타 업데이트(원하는 정책대로)
        track.setTitle(req.getTitle());
        track.setArtist(req.getArtist());
        track.setAlbum(req.getAlbum());
        track.setDurationSec(req.getDurationSec());
        track.setSourceType(req.getSourceType());
        track.setSourceUrl(req.getSourceUrl());

        //  sourceId 반영 필요 (아래 5번 엔티티 수정 참고)
        track.setSourceId(req.getSourceId());

        track = trackRepository.save(track);

        // 3) PlaylistTrack 중복 방지
        if (playlistTrackRepository.existsByPlaylistIdAndTrackId(playlistId, track.getId())) {
            throw new IllegalStateException("track already exists in playlist");
        }

        // 4) order = max + 1
        int nextOrder = playlistTrackRepository.findMaxOrder(playlistId) + 1;

        // 5) PlaylistTrack 생성/저장
        PlaylistTrack pt = PlaylistTrack.create(playlist, track, nextOrder);
        return playlistTrackRepository.save(pt);
    }
}
