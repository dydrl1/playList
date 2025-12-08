package com.playlist.backend.playlistTrack;

import com.playlist.backend.Track.Track;
import com.playlist.backend.Track.TrackRepository;
import com.playlist.backend.common.exception.BusinessException;
import com.playlist.backend.common.exception.ErrorCode;
import com.playlist.backend.playlist.Playlist;
import com.playlist.backend.playlist.PlaylistRepository;
import com.playlist.backend.playlist.dto.PlaylistResponse;
import com.playlist.backend.playlistTrack.dto.PlaylistAddTracksRequest;
import com.playlist.backend.playlistTrack.dto.PlaylistTrackResponse;
import com.playlist.backend.user.User;
import com.playlist.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaylistTrackService {

    private final PlaylistTrackRepository playlistTrackRepository;
    private final PlaylistRepository playlistRepository;
    private final UserRepository userRepository;
    private final TrackRepository trackRepository;

    // ==========================
    // 공통 검증
    // ==========================

    private Playlist getOwnedPlaylist(Long userId, Long playlistId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PLAYLIST_NOT_FOUND));

        if (!playlist.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        return playlist;
    }

    private Track getTrackOrThrow(Long trackId) {
        return trackRepository.findById(trackId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE));
    }

    // ==========================
    // 트랙 목록 조회
    // ==========================

    public List<PlaylistTrackResponse> getTracks(Long userId, Long playlistId) {
        Playlist playlist = getOwnedPlaylist(userId, playlistId);

        return playlistTrackRepository
                .findByPlaylistOrderByTrackOrderAsc(playlist)
                .stream()
                .map(PlaylistTrackResponse::from)
                .toList();
    }

    // ==========================
    // 트랙 추가
    // ==========================

    @Transactional
    public void addTrack(Long userId, Long playlistId, Long trackId, Integer trackOrder) {
        Playlist playlist = getOwnedPlaylist(userId, playlistId);
        Track track = getTrackOrThrow(trackId);

        PlaylistTrack playlistTrack = PlaylistTrack.builder()
                .playlist(playlist)
                .track(track)
                .trackOrder(trackOrder)
                .build();

        playlistTrackRepository.save(playlistTrack);
    }

    // ==========================
    // 트랙 삭제
    // ==========================

    @Transactional
    public void removeTrack(Long userId, Long playlistId, Long trackId) {
        Playlist playlist = getOwnedPlaylist(userId, playlistId);

        playlistTrackRepository.deleteByPlaylistIdAndTrackId(
                playlist.getId(),
                trackId
        );
    }
}

