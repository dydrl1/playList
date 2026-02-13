package com.playlist.backend.playlistTrack;

import com.playlist.backend.Track.Track;
import com.playlist.backend.Track.TrackRepository;
import com.playlist.backend.Track.dto.TrackSourceType;
import com.playlist.backend.common.exception.BusinessException;
import com.playlist.backend.common.exception.ErrorCode;
import com.playlist.backend.playlist.Playlist;
import com.playlist.backend.playlist.PlaylistRepository;
import com.playlist.backend.playlistTrack.dto.PlaylistTracksAddRequest;
import com.playlist.backend.playlistTrack.dto.PlaylistTrackReorderRequest;
import com.playlist.backend.playlistTrack.dto.PlaylistTrackResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaylistTrackService {

    private final PlaylistRepository playlistRepository;
    private final TrackRepository trackRepository;
    private final PlaylistTrackRepository playlistTrackRepository;

    /**
     * 트랙 목록 조회
     */
    public List<PlaylistTrackResponse> getTracks(Long userId, Long playlistId) {
        // 플레이리스트 소유자 검증
        Playlist playlist = getOwnedPlaylistOrThrow(userId, playlistId);

        List<PlaylistTrack> tracks =
                playlistTrackRepository.findByPlaylistIdOrderByTrackOrderAsc(playlistId);

        return tracks.stream()
                .map(PlaylistTrackResponse::from)
                .toList();
    }

    /**
     * 트랙 추가
     */
    @Transactional
    public void addTracks(Long playlistId, PlaylistTracksAddRequest req) {

        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PLAYLIST_NOT_FOUND));

        if (req.getTracks() == null || req.getTracks().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        for (PlaylistTracksAddRequest.TrackItem item : req.getTracks()) {
            String rawSourceType = item.getSourceType();
            String sourceType = rawSourceType == null ? null : rawSourceType.trim().toUpperCase();
            String sourceUrl = item.getSourceUrl();

            if (!TrackSourceType.isSupported(sourceType)) {
                throw new BusinessException(ErrorCode.TRACK_SOURCE_NOT_SUPPORTED);
            }

            if (sourceUrl == null || sourceUrl.isBlank()) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
            }

            Track track = trackRepository.findBySourceTypeAndSourceUrl(sourceType, sourceUrl)
                    .orElseGet(() -> {
                        Track t = new Track(item.getTitle(), item.getArtist(), item.getImageUrl());
                        t.setAlbum(item.getAlbum());
                        t.setDurationSec(item.getDurationSec());
                        t.setSourceType(sourceType);
                        t.setSourceUrl(sourceUrl);
                        return trackRepository.save(t);
                    });

            if (playlistTrackRepository.existsByPlaylistIdAndTrackId(playlistId, track.getId())) {
                throw new BusinessException(ErrorCode.PLAYLIST_TRACK_ALREADY_EXISTS);
            }

            int order;
            if (item.getTrackOrder() == null) {
                Integer max = playlistTrackRepository.findMaxOrder(playlistId);
                order = (max == null ? 1 : max + 1);
            } else {
                order = item.getTrackOrder();
            }

            // 1. PlaylistTrack 객체를 생성
            PlaylistTrack pt = PlaylistTrack.builder()
                    .playlist(playlist)
                    .track(track)
                    .trackOrder(order)
                    .build();

            // 2. ⭐ Playlist 엔티티 내부의 addTrack을 호출합니다.
            // 이 안에서 (1) 리스트에 추가 (2) 썸네일 없으면 업데이트 로직이 실행됩니다.
            playlist.addTrack(pt);

            // 3. 연결 정보를 저장합니다.
            playlistTrackRepository.save(pt);
        }
    }



    /**
     * 트랙 삭제
     */
    @Transactional
    public void removeTrack(Long userId, Long playlistId, Long trackId) {
        Playlist playlist = getOwnedPlaylistOrThrow(userId, playlistId);

        PlaylistTrack playlistTrack = playlistTrackRepository
                .findByPlaylistIdAndTrackId(playlistId, trackId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PLAYLIST_TRACK_NOT_FOUND));

        int removedOrder = playlistTrack.getTrackOrder();

        // 삭제
        playlistTrackRepository.delete(playlistTrack);

        // 뒤에 있던 애들 순서 -1
        List<PlaylistTrack> remainingTracks =
                playlistTrackRepository.findByPlaylistIdOrderByTrackOrderAsc(playlistId);

        for (PlaylistTrack pt : remainingTracks) {
            if (pt.getTrackOrder() > removedOrder) {
                pt.setTrackOrder(pt.getTrackOrder() - 1);
            }
        }
    }

    /**
     * 순서 변경
     */
    @Transactional
    public void reorderTracks(Long userId, Long playlistId, PlaylistTrackReorderRequest request) {
        Playlist playlist = getOwnedPlaylistOrThrow(userId, playlistId);

        List<Long> newOrderTrackIds = request.getTrackIds();
        if (newOrderTrackIds == null || newOrderTrackIds.isEmpty()) {
            throw new BusinessException(ErrorCode.PLAYLIST_TRACK_ORDER_INVALID);
        }

        List<PlaylistTrack> currentTracks =
                playlistTrackRepository.findByPlaylistIdOrderByTrackOrderAsc(playlistId);

        if (currentTracks.size() != newOrderTrackIds.size()) {
            throw new BusinessException(ErrorCode.PLAYLIST_TRACK_ORDER_MISMATCH);
        }

        Set<Long> currentTrackIdSet = currentTracks.stream()
                .map(pt -> pt.getTrack().getId())
                .collect(Collectors.toSet());

        Set<Long> requestedTrackIdSet = new HashSet<>(newOrderTrackIds);

        if (!currentTrackIdSet.equals(requestedTrackIdSet)) {
            throw new BusinessException(ErrorCode.PLAYLIST_TRACK_ORDER_MISMATCH);
        }

        Map<Long, PlaylistTrack> trackMap = currentTracks.stream()
                .collect(Collectors.toMap(pt -> pt.getTrack().getId(), Function.identity()));

        int order = 1;
        for (Long trackId : newOrderTrackIds) {
            PlaylistTrack pt = trackMap.get(trackId);
            pt.setTrackOrder(order++);
        }
    }

    /**
     * 플레이리스트 조회 + 소유자 검증
     */
    private Playlist getOwnedPlaylistOrThrow(Long userId, Long playlistId) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PLAYLIST_NOT_FOUND));

        if (!playlist.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        return playlist;
    }
}


