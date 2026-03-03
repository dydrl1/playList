package com.playlist.backend.playlist;

import com.playlist.backend.Track.Track;
import com.playlist.backend.Track.TrackRepository;
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
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
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
    private final TrackRepository trackRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final PlaylistViewCountRepository viewCountRepository;

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

    private void validateOwnerOrThrow(Long userId, Playlist playlist, ErrorCode errorCode) {
        Long ownerId = playlist.getUser().getId();
        if (!ownerId.equals(userId)) {
            throw new BusinessException(errorCode);
        }
    }

    private void validatePrivateAccess(Long loginUserId, Playlist playlist) {
        if (!playlist.isPublic()) {
            if (loginUserId == null) {
                throw new BusinessException(ErrorCode.PLAYLIST_PRIVATE);
            }
            // 로그인 했어도 소유자가 아니면 비공개 접근 불가
            validateOwnerOrThrow(loginUserId, playlist, ErrorCode.PLAYLIST_PRIVATE);
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
                .map(p -> {
                    // 내 플리 목록 조회 시, 내가 좋아요 눌렀는지 여부 판단
                    boolean isLiked = playlistLikeRepository.existsByPlaylistIdAndUserId(p.getId(), userId);
                    long totalView = p.getViewCount() + viewCountRepository.getCount(p.getId());
                    return PlaylistResponse.from(p, totalView, likeCountMap.getOrDefault(p.getId(), 0), isLiked);
                })
                .toList();
    }


    // =========================================
    //  플레이리스트 생성
    // =========================================

    @Transactional
    public PlaylistResponse createPlaylist(Long userId, PlaylistCreateRequest request) {
        log.info("요청된 유저 ID: {}", userId);
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
        return PlaylistResponse.from(saved, 0L, 0, false);
    }

    // =========================================
    //  플레이리스트 수정
    // =========================================
    @Transactional
    public PlaylistResponse updateMyPlaylist(Long userId, Long playlistId, PlaylistUpdateRequest request) {
        Playlist playlist = getPlaylistOrThrow(playlistId);

        validateOwnerOrThrow(userId, playlist, ErrorCode.PLAYLIST_MODIFY_FORBIDDEN);

        playlist.update(
                request.getTitle(),
                request.getDescription(),
                request.getIsPublic(),
                request.getThumbnailUrl()
        );

        int likeCount = (int) playlistLikeRepository.countByPlaylistId(playlistId);
        boolean isLiked = playlistLikeRepository.existsByPlaylistIdAndUserId(playlistId, userId);
        long totalView = playlist.getViewCount() + viewCountRepository.getCount(playlist.getId());
        return PlaylistResponse.from(playlist, totalView, likeCount, isLiked);
    }

    // =========================================
    //  플레이리스트 삭제
    // =========================================
    @Transactional
    public void deleteMyPlaylist(Long userId, Long playlistId) {
        Playlist playlist = getPlaylistOrThrow(playlistId);

        validateOwnerOrThrow(userId, playlist, ErrorCode.PLAYLIST_DELETE_FORBIDDEN);

        playlistRepository.delete(playlist);
    }

    // =========================================
    //  플레이리스트 상세 보기
    // =========================================
    @Transactional
    public PlaylistDetailResponse getDetail(Long playlistId, Long loginUserId) {
        Playlist playlist = getPlaylistOrThrow(playlistId);

        // 1. 조회수 로직 (중복 방지 포함된 레포지토리 호출)
        viewCountRepository.incrementIfFirstVisit(playlistId, loginUserId);

        // 2. 최신 합산 조회수 계산
        long latestViewCount = playlist.getViewCount() + viewCountRepository.getCount(playlistId);

        // 3. 비공개 체크 로직
        checkAccessAuthority(playlist, loginUserId);

        // 4. 나머지 트랙/좋아요 정보 조회
        List<TrackItemResponse> tracks = getTracks(playlistId);
        int likeCount = getLikeCount(playlistId);
        boolean likedByMe = isLikedByMe(playlistId, loginUserId);

        return PlaylistDetailResponse.of(playlist, latestViewCount, likeCount, likedByMe, tracks, loginUserId);
    }

    private void checkAccessAuthority(Playlist playlist, Long userId) {
        if (!playlist.isPublic()) {
            if (userId == null) throw new BusinessException(ErrorCode.PLAYLIST_PRIVATE);
            validatePrivateAccess(userId, playlist);
        }
    }

    /**
     * 트랙 목록 조회 (Private Helper)
     */
    private List<TrackItemResponse> getTracks(Long playlistId) {
        return playlistTrackRepository.findQueueByPlaylistId(playlistId)
                .stream()
                .map(TrackItemResponse::from)
                .toList();
    }

    /**
     * 총 좋아요 수 조회 (Private Helper)
     */
    private int getLikeCount(Long playlistId) {
        return (int) playlistLikeRepository.countByPlaylistId(playlistId);
    }

    /**
     * 로그인 사용자의 좋아요 여부 확인 (Private Helper)
     */
    private boolean isLikedByMe(Long playlistId, Long userId) {
        return (userId != null) && playlistLikeRepository.existsByPlaylistIdAndUserId(playlistId, userId);
    }


    // @Cacheable(value = "publicPlaylists", key = "#sort.name() + ':' + #pageable.pageNumber", cacheManager = "cacheManager")
    @Transactional(readOnly = true)
    public Page<PlaylistResponse> getPublicPlaylists(PublicPlaylistSort sort, Pageable pageable, Long loginUserId) {
        // 1. 기본 페이지 조회
        Page<Playlist> page = switch (sort) {
            case VIEW -> playlistRepository.findByIsPublicTrueOrderByViewCountDescIdDesc(pageable);
            case LATEST -> playlistRepository.findByIsPublicTrueOrderByIdDesc(pageable);
            default -> throw new BusinessException(ErrorCode.INVALID_REQUEST);
        };

        if (page.isEmpty()) return Page.empty(pageable);

        List<Long> ids = page.getContent().stream().map(Playlist::getId).toList();

        // 2. 좋아요 수 일괄 조회 (이미 하신 부분)
        Map<Long, Integer> likeCountMap = getLikeCountMap(ids);

        // 3. [개선] 내가 좋아요 누른 ID 목록 한 번에 조회 (In-절)
        Set<Long> likedPlaylistIds = new HashSet<>();
        if (loginUserId != null) {
            likedPlaylistIds = new HashSet<>(
                    playlistLikeRepository.findAllPlaylistIdsByUserIdAndPlaylistIdsIn(loginUserId, ids)
            );
        }

        final Set<Long> finalLikedIds = likedPlaylistIds;
        return page.map(p -> {
            boolean isLiked = finalLikedIds.contains(p.getId());
            // [추가] 앞서 만든 Redis 조회수 합산 로직을 여기서 적용
            //long totalViewCount = p.getViewCount() + viewCountRepository.getCount(p.getId());
            long totalViewCount = p.getViewCount() + 0;
            return PlaylistResponse.from(p, totalViewCount, likeCountMap.getOrDefault(p.getId(), 0), isLiked);
        });
    }


    // 좋아요 순 전체보기
    @Transactional(readOnly = true)
    public Page<PlaylistResponse> getPublicPlaylistsOrderByLike(Pageable pageable, Long loginUserId) {
        // 1. 좋아요 순 페이징 조회 (DB 쿼리)
        var page = playlistRepository.findPublicOrderByLikeCountDesc(pageable);
        if (page.isEmpty()) return Page.empty(pageable);

        // 2. 현재 페이지의 ID 리스트 추출
        List<Long> ids = page.getContent().stream().map(Playlist::getId).toList();

        // 3. 좋아요 수 맵 생성 (일괄 조회)
        Map<Long, Integer> likeCountMap = playlistLikeRepository.countGroupByPlaylistIds(ids).stream()
                .collect(Collectors.toMap(
                        PlaylistLikeCountRow::getPlaylistId,
                        row -> row.getCnt().intValue()
                ));

        // 4. [개선] 로그인 유저의 좋아요 여부 일괄 조회 (N+1 방지)
        Set<Long> likedIds = (loginUserId != null)
                ? new HashSet<>(playlistLikeRepository.findAllPlaylistIdsByUserIdAndPlaylistIdsIn(loginUserId, ids))
                : Collections.emptySet();

        // 5. 결과 조합 및 DTO 생성 (4개 인자 사용)
        return page.map(p -> {
            // 메모리(Set)에서 좋아요 여부 확인
            boolean isLiked = likedIds.contains(p.getId());

            // Redis 실시간 조회수 합산
            long totalViewCount = p.getViewCount() + viewCountRepository.getCount(p.getId());

            // 수정된 PlaylistResponse.from 호출 (Playlist, viewCount, likeCount, isLiked)
            return PlaylistResponse.from(
                    p,
                    totalViewCount,
                    likeCountMap.getOrDefault(p.getId(), 0),
                    isLiked
            );
        });
    }



    // 로그인 유저 기준 24시간 유니크 조회수
    @Transactional
    public void increaseUniqueView(Long playlistId, Long loginUserId) {
        if (loginUserId == null) return;

        try {
            String key = "view:playlist:" + playlistId + ":user:" + loginUserId;

            Boolean first = stringRedisTemplate.opsForValue()
                    .setIfAbsent(key, "1", Duration.ofHours(24));

            if (Boolean.TRUE.equals(first)) {
                playlistRepository.increaseViewCount(playlistId);
            }
        } catch (Exception e) {
            // Redis가 꺼져있으면 조회수 기능만 비활성화 (서버는 정상 동작)
            log.warn("Redis unavailable. Skip unique view. playlistId={}, userId={}", playlistId, loginUserId);
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

    // 중복되는 좋아요 집계 로직 추출
    private Map<Long, Integer> getLikeCountMap(List<Long> ids) {
        return playlistLikeRepository.countGroupByPlaylistIds(ids).stream()
                .collect(Collectors.toMap(
                        PlaylistLikeCountRow::getPlaylistId,
                        row -> row.getCnt().intValue()
                ));
    }

    @Transactional
    public void addTracksToPlaylist(Long playlistId, List<TrackAddRequest> requests, Long userId) {
        Playlist playlist = getPlaylistOrThrow(playlistId);
        validateOwnerOrThrow(userId, playlist, ErrorCode.ACCESS_DENIED);

        for (TrackAddRequest req : requests) {
            // 1. 이미 DB에 있는 곡인지 확인 (Repository 활용)
            Track track = trackRepository.findBySourceTypeAndSourceId(req.getSourceType(), req.getSourceUrl())
                    .orElseGet(() -> {
                        // 2. 없는 곡이면 새로 생성하여 저장
                        Track newTrack = Track.builder()
                                .title(req.getTitle())
                                .artist(req.getArtist())
                                .imageUrl(req.getImageUrl())
                                .sourceType(req.getSourceType())
                                .sourceId(req.getSourceUrl()) // Request의 sourceUrl을 sourceId로 매핑
                                .build();
                        return trackRepository.save(newTrack);
                    });

            // 3. PlaylistTrack 연결 객체 생성
            PlaylistTrack playlistTrack = PlaylistTrack.builder()
                    .playlist(playlist)
                    .track(track)
                    .build();

            // 4. ⭐ Playlist 엔티티의 addTrack 호출 (여기서 썸네일 업데이트 발생!)
            playlist.addTrack(playlistTrack);

            // 5. 연결 정보 저장
            playlistTrackRepository.save(playlistTrack);
        }
    }


    @Transactional
    public void removeTrackFromPlaylist(Long playlistTrackId, Long loginUserId) {
        PlaylistTrack pt = playlistTrackRepository.findById(playlistTrackId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRACK_NOT_FOUND));

        Playlist playlist = pt.getPlaylist(); // 플레이리스트 객체 확보

        if (!playlist.getUser().getId().equals(loginUserId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        // 3. 연결 삭제
        playlistTrackRepository.delete(pt);

        // 4. 썸네일 동기화 (삭제 후처리)
        // 만약 삭제된 트랙의 이미지가 현재 플레이리스트의 썸네일과 같다면 갱신
        if (pt.getTrack().getImageUrl().equals(playlist.getThumbnailUrl())) {
            // 남은 트랙 중 첫 번째 트랙의 이미지로 변경 (없으면 null)
            List<PlaylistTrack> remaining = playlistTrackRepository.findQueueByPlaylistId(playlist.getId());
            String nextThumb = remaining.isEmpty() ? null : remaining.get(0).getTrack().getImageUrl();
            playlist.updateThumbnail(nextThumb); // 아까 만든 메서드 활용
        }
    }
}
