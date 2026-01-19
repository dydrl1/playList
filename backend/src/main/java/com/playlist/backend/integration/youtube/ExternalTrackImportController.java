package com.playlist.backend.integration.youtube;

import com.playlist.backend.integration.dto.TrackImportRequest;
import com.playlist.backend.playlistTrack.PlaylistTrack;
import com.playlist.backend.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/playlists")
public class ExternalTrackImportController {

    private final ExternalTrackImportService importService;

    @PostMapping("/{playlistId}/tracks/import")
    public ResponseEntity<?> importTrack(
            @PathVariable Long playlistId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody TrackImportRequest request
    ) {
        Long userId = userDetails.getId();
        PlaylistTrack saved = importService.importToPlaylist(playlistId, userId, request);
        return ResponseEntity.ok(saved.getId()); // 응답 DTO로 바꿔도 됨
    }
}
