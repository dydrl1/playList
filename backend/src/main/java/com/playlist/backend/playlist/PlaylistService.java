package com.playlist.backend.playlist;

import com.playlist.backend.common.exception.BusinessException;
import com.playlist.backend.common.exception.ErrorCode;
import com.playlist.backend.playlist.dto.*;
import com.playlist.backend.playlist.dto.PlaylistResponse;
import com.playlist.backend.playlistLike.PlaylistLike;
import com.playlist.backend.playlistLike.PlaylistLikeCountRow;
import com.playlist.backend.playlistLike.PlaylistLikeRepository;
import com.playlist.backend.playlistTrack.PlaylistTrack;
import com.playlist.backend.playlistTrack.PlaylistTrackRepository;
import com.playlist.backend.user.User;
import com.playlist.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private final StringRedisTemplate stringRedisTemplate;

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
    //  내 플레이리스트 전체 조회 (좋아요 집계 1쿼리 + 정렬 정책 명시)
    // =========================================
    @Transactional(readOnly = true)
    public List<PlaylistResponse> getMyPlaylists(Long userId) {
        User owner = getUserOrThrow(userId);

        // 정렬 정책 명시(최신순) Repository 메서드 추가 필요 (아래 참고)
        List<Playlist> playlists = playlistRepository.findAllByUserIdOrderByIdDesc(owner.getId());

        // 플레이리스트가 없으면 바로 반환
        if (playlists.isEmpty()) {
            return List.of();
        }

        // 1) id 목록 추출
        List<Long> ids = playlists.stream()
                .map(Playlist::getId)
                .toList();

        // 좋아요 집계 1번 쿼리
        Map<Long, Integer> likeCountMap = playlistLikeRepository.countGroupByPlaylistIds(ids).stream()
                .collect(Collectors.toMap(
                        PlaylistLikeCountRow::getPlaylistId,
                        row -> row.getCnt().intValue()
                ));

        // 3) DTO 매핑 (없으면 0)
        return playlists.stream()
                .map(p -> PlaylistResponse.from(p, likeCountMap.getOrDefault(p.getId(), 0)))
                .toList();
    }



    // =========================================
    //  내 플레이리스트 단건 조회
    // =========================================
    @Transactional(readOnly = true)
    public PlaylistResponse getMyPlaylist(Long userId, Long playlistId) {
        Playlist playlist = getPlaylistOrThrow(playlistId);
        validateOwner(userId, playlist);

        int likeCount = (int) playlistLikeRepository.countByPlaylistId(playlistId);

        return PlaylistResponse.from(playlist, likeCount);
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
        return PlaylistResponse.from(saved, 0);
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

        // 현재 좋아요 수 조회
        int likeCount = (int) playlistLikeRepository.countByPlaylistId(playlistId);

        // dirty checking 으로 자동 반영
        return PlaylistResponse.from(playlist, likeCount);
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


    // =========================================
    //  플레이리스트 상세 보기
    // =========================================
    @Transactional
    public PlaylistDetailResponse getDetail(Long playlistId, Long loginUserId) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PLAYLIST_NOT_FOUND));

        // 트랙: fetch join(또는 entity graph)로 1 쿼리
        List<TrackItemResponse> tracks = playlistTrackRepository
                .findQueueByPlaylistId(playlistId)
                .stream()
                .map(TrackItemResponse::from)
                .toList();
        // 좋아요 수 : count 1쿼리
        int likeCount = (int) playlistLikeRepository.countByPlaylistId(playlistId);

        // LikeByMe: exists 1쿼리
        boolean likedByMe = (loginUserId != null)
                && playlistLikeRepository.existsByPlaylistIdAndUserId(playlistId, loginUserId);

        return PlaylistDetailResponse.of(playlist, likeCount, likedByMe, tracks);
    }


    // 로그인 유저 기준 24시간 유니크 조회수
    @Transactional
    public void increaseUniqueView(Long playlistId, Long loginUserId) {
        if (loginUserId == null) return; // 로그인 유저만 유니크 조회수

        String key = "view:playlist:" + playlistId + ":user:" + loginUserId;

        Boolean first = stringRedisTemplate.opsForValue()
                .setIfAbsent(key, "1", Duration.ofHours(24));

        if (Boolean.TRUE.equals(first)) {
            playlistRepository.increaseViewCount(playlistId);
        }
    }


    // 좋아요 누르기
    public void like(Long userId, Long playlistId) {
        // 존재 검증(없으면 명확한 에러)
        User user = getUserOrThrow(userId);
        Playlist playlist = getPlaylistOrThrow(playlistId);

        // 멱등
        if (playlistLikeRepository.existsByPlaylistIdAndUserId(playlistId, userId)) {
            return;
        }

        playlistLikeRepository.save(new PlaylistLike(user, playlist));
    }

    // 좋아요 취소
    public void unlike(Long userId, Long playlistId) {
        // 존재 검증(없으면 명확한 에러)
        getUserOrThrow(userId);
        getPlaylistOrThrow(playlistId);

        PlaylistLike like = playlistLikeRepository
                .findByUserIdAndPlaylistId(userId, playlistId)
                .orElse(null);

        if (like == null) return; // 멱등
        playlistLikeRepository.delete(like);
    }

}
