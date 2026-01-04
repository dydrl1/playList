package com.playlist.backend.playlist;

import com.playlist.backend.common.exception.BusinessException;
import com.playlist.backend.common.exception.ErrorCode;
import com.playlist.backend.playlist.dto.*;
import com.playlist.backend.playlist.dto.PlaylistResponse;
import com.playlist.backend.playlistLike.PlaylistLike;
import com.playlist.backend.playlistLike.PlaylistLikeRepository;
import com.playlist.backend.playlistTrack.PlaylistTrack;
import com.playlist.backend.playlistTrack.PlaylistTrackRepository;
import com.playlist.backend.user.User;
import com.playlist.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final UserRepository userRepository;
    private final PlaylistTrackRepository playlistTrackRepository;
    private final PlaylistLikeRepository playlistLikeRepository;

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
        log.info("createPlaylist userId={}", userId);

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


    // @@@@@@@@@@@@@@@ 상세 보기 @@@@@@@@@@@@@
    @Transactional(readOnly = true)
    public PlaylistDetailResponse getDetail(Long playlistId, Long loginUserId) {

        // 1) Playlist 기본 정보만 조회
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PLAYLIST_NOT_FOUND));

        // 2) Tracks 조회 (track까지 한번에)
        List<TrackItemResponse> tracks = playlistTrackRepository
                .findAllWithTrackByPlaylistId(playlistId)
                .stream()
                .map(TrackItemResponse::from)
                .toList();

        // 3) Likes는 컬렉션 로딩 대신 count/exists
        int likeCount = (int) playlistLikeRepository.countByPlaylistId(playlistId);

        boolean likedByMe = (loginUserId != null)
                && playlistLikeRepository.existsByPlaylistIdAndUserId(playlistId, loginUserId);

        return PlaylistDetailResponse.of(playlist, likeCount, likedByMe, tracks);
    }

}
