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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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
                    return PlaylistResponse.from(p, likeCountMap.getOrDefault(p.getId(), 0), isLiked);
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
        return PlaylistResponse.from(saved, 0, false);
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
        return PlaylistResponse.from(playlist, likeCount, isLiked);
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

        // 1. 조회수 중복 방지 로직 (상단에 배치)
        increaseViewCountWithRedis(playlist, loginUserId);

        // 비공개 접근 제어
        if(!playlist.isPublic()){
            if(loginUserId == null){
                throw new BusinessException(ErrorCode.PLAYLIST_PRIVATE);
            }
            validatePrivateAccess(loginUserId, playlist);
        }

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

        return PlaylistDetailResponse.of(playlist, likeCount, likedByMe, tracks, loginUserId);
    }

    private void increaseViewCountWithRedis(Playlist playlist, Long userId) {
        // 비로그인 사용자는 IP 등으로 식별하거나, 일단 로그인을 기준으로 설명합니다.
        String userKey = (userId != null) ? userId.toString() : "guest";
        String redisKey = "view:playlist:" + playlist.getId() + ":user:" + userKey;

        try {
            // Redis에 키가 없으면 처음 조회하는 것 (24시간 유효)
            Boolean isFirstView = redisTemplate.opsForValue().setIfAbsent(redisKey, "true", 24, TimeUnit.HOURS);

            if (Boolean.TRUE.equals(isFirstView)) {
                playlist.incrementViewCount(); // 이 메서드 안에서 viewCount++ 실행
            }
        } catch (Exception e) {
            // Redis가 죽었을 때 발생하는 경고 (로그에 찍혔던 상황)
            log.warn("Redis unavailable. Skip unique view. playlistId={}, userId={}", playlist.getId(), userId);

            // [선택] Redis가 없으면 그냥 무조건 올릴지, 아니면 안 올릴지 결정
            // playlist.incrementViewCount(); // Redis 없어도 일단 올리려면 주석 해제
        }
    }


    // 최신/조회순 전체 보기
    @Transactional(readOnly = true)
    public Page<PlaylistResponse> getPublicPlaylists(PublicPlaylistSort sort, Pageable pageable, Long loginUserId) {
        Page<Playlist> page = switch (sort) {
            case VIEW -> playlistRepository.findByIsPublicTrueOrderByViewCountDescIdDesc(pageable);
            case LATEST -> playlistRepository.findByIsPublicTrueOrderByIdDesc(pageable);
            default -> throw new BusinessException(ErrorCode.INVALID_REQUEST);
        };

        if (page.isEmpty()) return Page.empty(pageable);

        List<Long> ids = page.getContent().stream().map(Playlist::getId).toList();
        Map<Long, Integer> likeCountMap = getLikeCountMap(ids);

        return page.map(p -> {
            boolean isLiked = (loginUserId != null) && playlistLikeRepository.existsByPlaylistIdAndUserId(p.getId(), loginUserId);
            return PlaylistResponse.from(p, likeCountMap.getOrDefault(p.getId(), 0), isLiked);
        });
    }


    // 좋아요 순 전체보기
    @Transactional(readOnly = true)
    public Page<PlaylistResponse> getPublicPlaylistsOrderByLike(Pageable pageable, Long loginUserId) {
        var page = playlistRepository.findPublicOrderByLikeCountDesc(pageable);
        if (page.isEmpty()) return Page.empty(pageable);

        List<Long> ids = page.getContent().stream().map(Playlist::getId).toList();

        Map<Long, Integer> likeCountMap = playlistLikeRepository.countGroupByPlaylistIds(ids).stream()
                .collect(Collectors.toMap(
                        PlaylistLikeCountRow::getPlaylistId,
                        row -> row.getCnt().intValue()
                ));

        return page.map(p -> {
            boolean isLiked = (loginUserId != null) && playlistLikeRepository.existsByPlaylistIdAndUserId(p.getId(), loginUserId);
            return PlaylistResponse.from(p, likeCountMap.getOrDefault(p.getId(), 0), isLiked);
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
