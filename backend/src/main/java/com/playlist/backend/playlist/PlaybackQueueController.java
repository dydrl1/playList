package com.playlist.backend.playlist;

import com.playlist.backend.common.response.ApiResponse;
import com.playlist.backend.playlist.dto.PlaybackQueueResponse;
import com.playlist.backend.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/playlists")
public class PlaybackQueueController {

    private final PlaybackQueueService playbackQueueService;

    @GetMapping("/{playlistId}/queue")
    public ResponseEntity<ApiResponse<PlaybackQueueResponse>> getQueue(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long playlistId
    ){
        Long loginUserId = (userDetails != null) ? userDetails.getId() : null;
        PlaybackQueueResponse result = playbackQueueService.getQueue(playlistId, loginUserId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
