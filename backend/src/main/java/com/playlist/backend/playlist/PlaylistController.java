package com.playlist.backend.playlist;

import com.playlist.backend.common.response.ApiResponse;
import com.playlist.backend.playlist.dto.*;
import com.playlist.backend.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PlaylistController {

    private final PlaylistService playlistService;

    /**
     * 내 플레이리스트 목록 조회
     */
    @GetMapping("/me/playlists")
    public ResponseEntity<ApiResponse<List<PlaylistResponse>>> getMyPlaylists(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<PlaylistResponse> result = playlistService.getMyPlaylists(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    /**
     *  플레이리스트 생성
     */
    @PostMapping("/me/playlists")
    public ResponseEntity<ApiResponse<PlaylistResponse>> createPlaylist(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody PlaylistCreateRequest request
    ) {
        PlaylistResponse result = playlistService.createPlaylist(userDetails.getId(), request);
        log.info("createPlaylist login userId={}", userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     *  플레이리스트 수정
     */
    @PatchMapping("/me/playlists/{playlistId}")
    public ResponseEntity<ApiResponse<PlaylistResponse>> updateMyPlaylist(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long playlistId,
            @RequestBody PlaylistUpdateRequest request
    ) {
        PlaylistResponse result = playlistService.updateMyPlaylist(userDetails.getId(), playlistId, request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     *  플레이리스트 삭제
     */
    @DeleteMapping("/me/playlists/{playlistId}")
    public ResponseEntity<ApiResponse<String>> deleteMyPlaylist(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long playlistId
    ) {
        playlistService.deleteMyPlaylist(userDetails.getId(), playlistId);
        return ResponseEntity.ok(ApiResponse.success("플레이리스트가 삭제되었습니다."));
    }


    // =========================
    // 공개 영역 (목록/상세)
    // =========================

    /**
     * 공개 플레이리스트 목록 조회 (로그인/비로그인 모두 가능)
     * GET /api/playlists?sort=LATEST|VIEW|LIKE&page=0&size=20
     */
    @GetMapping("/playlists")
    public ResponseEntity<ApiResponse<Page<PlaylistResponse>>> getPublicPlaylists(
            @RequestParam(defaultValue = "LATEST") PublicPlaylistSort sort,
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Long loginUserId = (user != null) ? user.getId() : null; // 비로그인 대응
        // pageable에서 잘못된 정렬 정보를 제거하고 새 PageRequest 생성
        Pageable cleanPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());

        Page<PlaylistResponse> page = switch (sort){
            case LIKE -> playlistService.getPublicPlaylistsOrderByLike(cleanPageable, loginUserId);
            case VIEW, LATEST -> playlistService.getPublicPlaylists(sort,cleanPageable, loginUserId);
        };

        return ResponseEntity.ok(ApiResponse.success(page));
    }


    /**
     * 플레이리스트 상세 조회
     * - 공개: 로그인/비로그인 모두 가능
     * - 비공개: 오너만 가능
     */
    @GetMapping("/playlists/{playlistId}")
    public ResponseEntity<ApiResponse<PlaylistDetailResponse>> getPlaylistDetail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long playlistId
    ) {
        Long loginUserId = (userDetails != null) ? userDetails.getId() : null;

        // 1) 접근 제어 포함된 상세 조회 (여기서 비공개면 예외)
        PlaylistDetailResponse result = playlistService.getDetail(playlistId, loginUserId);

        // 2) 조회 성공한 경우에만 조회수 증가
        playlistService.increaseUniqueView(playlistId, loginUserId);

        return ResponseEntity.ok(ApiResponse.success(result));
    }


    /**
     *  플레이리스트 좋아요
     */
    @PostMapping("playlists/{playlistId}/likes")
    public ResponseEntity<Void> like(
            @PathVariable Long playlistId,
            @AuthenticationPrincipal CustomUserDetails user
    ){
        playlistService.like(user.getId(), playlistId);
        return ResponseEntity.noContent().build();
    }


    /**
     *  플레이리스트 좋아요 취소
     */

    @DeleteMapping("playlists/{playlistId}/unlikes")
    public ResponseEntity<Void> unlike(
            @PathVariable Long playlistId,
            @AuthenticationPrincipal CustomUserDetails user
    ){
        playlistService.unlike(user.getId(), playlistId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/playlists/tracks/{playlistTrackId}")
    public ResponseEntity<ApiResponse<String>> removeTrack(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long playlistTrackId
    ) {
        playlistService.removeTrackFromPlaylist(playlistTrackId, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("플레이리스트에서 곡이 제거되었습니다."));
    }
}
