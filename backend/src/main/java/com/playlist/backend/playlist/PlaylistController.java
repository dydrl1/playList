package com.playlist.backend.playlist;

import com.playlist.backend.common.response.ApiResponse;
import com.playlist.backend.playlist.dto.PlaylistCreateRequest;
import com.playlist.backend.playlist.dto.PlaylistDetailResponse;
import com.playlist.backend.playlist.dto.PlaylistResponse;
import com.playlist.backend.playlist.dto.PlaylistUpdateRequest;
import com.playlist.backend.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/playlists")
@RequiredArgsConstructor
public class PlaylistController {

    private final PlaylistService playlistService;

    /**
     * 내 플레이리스트 목록 조회
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<PlaylistResponse>>> getMyPlaylists(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<PlaylistResponse> result = playlistService.getMyPlaylists(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     *  내 플레이리스트 상세 조회
     */
    @GetMapping("/{playlistId}")
    public ResponseEntity<ApiResponse<PlaylistDetailResponse>> getPlaylistDetail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long playlistId
    ) {
        Long loginUserId = (userDetails != null) ? userDetails.getId() : null;
        PlaylistDetailResponse result = playlistService.getDetail(playlistId, loginUserId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     *  플레이리스트 생성
     */
    @PostMapping("/me")
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
    @PatchMapping("/me/{playlistId}")
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
    @DeleteMapping("/me/{playlistId}")
    public ResponseEntity<ApiResponse<String>> deleteMyPlaylist(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long playlistId
    ) {
        playlistService.deleteMyPlaylist(userDetails.getId(), playlistId);
        return ResponseEntity.ok(ApiResponse.success("플레이리스트가 삭제되었습니다."));
    }

//    /**
//     *  상세보기
//     */
//    @GetMapping("/{playlistId}")
//    public PlaylistDetailResponse getPlaylistDetail(@PathVariable Long playlistId){
//        return playlistService.getDetail(playlistId, null);
//    }
}
