package com.playlist.backend.playlistTrack;

import com.playlist.backend.common.response.ApiResponse;
import com.playlist.backend.playlistTrack.PlaylistTrackService;
import com.playlist.backend.playlistTrack.dto.PlaylistTrackResponse;
import com.playlist.backend.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/playlists/me/{playlistId}/tracks")
public class PlaylistTrackController {

    private final PlaylistTrackService playlistTrackService;

    // 트랙 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponse<List<PlaylistTrackResponse>>> getTracks(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long playlistId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        playlistTrackService.getTracks(userDetails.getId(), playlistId)
                )
        );
    }

    // 트랙 추가
    @PostMapping
    public ResponseEntity<ApiResponse<String>> addTrack(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long playlistId,
            @RequestParam Long trackId,
            @RequestParam Integer trackOrder
    ) {
        playlistTrackService.addTrack(
                userDetails.getId(),
                playlistId,
                trackId,
                trackOrder
        );
        return ResponseEntity.ok(ApiResponse.success("트랙이 추가되었습니다."));
    }

    // 트랙 삭제
    @DeleteMapping("/{trackId}")
    public ResponseEntity<ApiResponse<String>> removeTrack(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long playlistId,
            @PathVariable Long trackId
    ) {
        playlistTrackService.removeTrack(
                userDetails.getId(),
                playlistId,
                trackId
        );
        return ResponseEntity.ok(ApiResponse.success("트랙이 삭제되었습니다."));
    }
}
