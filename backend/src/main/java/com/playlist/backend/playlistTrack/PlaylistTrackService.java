package com.playlist.backend.playlistTrack;

import com.playlist.backend.Track.Track;
import com.playlist.backend.Track.TrackRepository;
import com.playlist.backend.common.exception.BusinessException;
import com.playlist.backend.common.exception.ErrorCode;
import com.playlist.backend.playlist.Playlist;
import com.playlist.backend.playlist.PlaylistRepository;
import com.playlist.backend.playlistTrack.dto.PlaylistTrackReorderRequest;
import com.playlist.backend.playlistTrack.dto.PlaylistTrackResponse;
import com.playlist.backend.user.UserRepository;
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
    public void addTrack(Long userId, Long playlistId, Long trackId, Integer trackOrder) {
        Playlist playlist = getOwnedPlaylistOrThrow(userId, playlistId);

        Track track = trackRepository.findById(trackId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRACK_NOT_FOUND));

        // 이미 존재하는 트랙인지 체크
        boolean exists = playlistTrackRepository.existsByPlaylistIdAndTrackId(playlistId, trackId);
        if (exists) {
            throw new BusinessException(ErrorCode.PLAYLIST_TRACK_ALREADY_EXISTS);
        }

        // 현재 트랙들 순서 조회
        List<PlaylistTrack> currentTracks =
                playlistTrackRepository.findByPlaylistIdOrderByTrackOrderAsc(playlistId);

        int size = currentTracks.size();

        // trackOrder 검증 (1 ~ size+1)
        if (trackOrder == null || trackOrder < 1 || trackOrder > size + 1) {
            throw new BusinessException(ErrorCode.PLAYLIST_TRACK_ORDER_INVALID);
        }

        // 지정한 순서부터 뒤에 있는 애들 순서 +1
        for (PlaylistTrack pt : currentTracks) {
            if (pt.getTrackOrder() >= trackOrder) {
                pt.setTrackOrder(pt.getTrackOrder() + 1);
            }
        }

        PlaylistTrack newPlaylistTrack = new PlaylistTrack(playlist, track, trackOrder);
        playlistTrackRepository.save(newPlaylistTrack);
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


