package com.playlist.backend.playlist;

import com.playlist.backend.common.exception.BusinessException;
import com.playlist.backend.common.exception.ErrorCode;
import com.playlist.backend.playlist.dto.PlaylistCreateRequest;
import com.playlist.backend.playlist.dto.PlaylistResponse;
import com.playlist.backend.playlist.dto.PlaylistUpdateRequest;
import com.playlist.backend.user.User;
import com.playlist.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final UserRepository userRepository;

    // =========================================
    //  유틸 메서드 (검증용)
    // =========================================

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private Playlist getPlaylistOrThrow(Long playlistId) {
        return playlistRepository.findById(playlistId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PLAYLIST_NOT_FOUND));
    }

    private void validateOwner(Long userId, Playlist playlist) {
        if (!playlist.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
    }

    // =========================================
    //  내 플레이리스트 전체 조회
    // =========================================

    public List<PlaylistResponse> getMyPlaylists(Long userId) {
        // 유저 존재 여부 검증 (없으면 404)
        User owner = getUserOrThrow(userId);

        return playlistRepository.findAllByUserId(owner.getId()).stream()
                .map(PlaylistResponse::from)
                .toList();
    }

    // =========================================
    //  내 플레이리스트 단건 조회
    // =========================================

    public PlaylistResponse getMyPlaylist(Long userId, Long playlistId) {
        Playlist playlist = getPlaylistOrThrow(playlistId);
        validateOwner(userId, playlist);

        return PlaylistResponse.from(playlist);
    }

    // =========================================
    //  플레이리스트 생성
    // =========================================

    @Transactional
    public PlaylistResponse createPlaylist(Long userId, PlaylistCreateRequest request) {
        User owner = getUserOrThrow(userId);

        boolean isPublic = request.getIsPublic() == null || request.getIsPublic();

        Playlist playlist = Playlist.builder()
                .user(owner)
                .title(request.getTitle())
                .description(request.getDescription())
                .isPublic(isPublic)
                .build();

        Playlist saved = playlistRepository.save(playlist);
        return PlaylistResponse.from(saved);
    }

    // =========================================
    //  플레이리스트 수정
    // =========================================

    @Transactional
    public PlaylistResponse updateMyPlaylist(Long userId, Long playlistId, PlaylistUpdateRequest request) {
        Playlist playlist = getPlaylistOrThrow(playlistId);
        validateOwner(userId, playlist);

        playlist.update(
                request.getTitle(),
                request.getDescription(),
                request.getIsPublic()
        );

        // dirty checking 으로 자동 반영
        return PlaylistResponse.from(playlist);
    }

    // =========================================
    //  플레이리스트 삭제
    // =========================================

    @Transactional
    public void deleteMyPlaylist(Long userId, Long playlistId) {
        Playlist playlist = getPlaylistOrThrow(playlistId);
        validateOwner(userId, playlist);

        playlistRepository.delete(playlist);
    }
}
