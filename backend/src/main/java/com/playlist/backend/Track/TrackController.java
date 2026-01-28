package com.playlist.backend.Track;

import com.playlist.backend.Track.dto.TrackCreateRequest;
import com.playlist.backend.Track.dto.TrackResponse;
import com.playlist.backend.Track.dto.TrackUpdateRequest;
import com.playlist.backend.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/tracks")
public class TrackController {

    private final TrackService trackService;

    // 목록 조회 (+ 검색)
    @GetMapping
    public Page<TrackResponse> getTracks(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return trackService.getTracks(keyword, pageable);
    }

    // 단건 조회
    @GetMapping("/{trackId}")
    public ResponseEntity<ApiResponse<TrackResponse>> getTrack(@PathVariable Long trackId) {
        return ResponseEntity.ok(
                ApiResponse.success(trackService.getTrack(trackId))
        );
    }

    // 생성
    @PostMapping
    public ResponseEntity<ApiResponse<TrackResponse>> createTrack(
            @RequestBody TrackCreateRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(trackService.createTrack(request))
        );
    }

    // 수정 (PATCH)
    @PatchMapping("/{trackId}")
    public ResponseEntity<ApiResponse<TrackResponse>> updateTrack(
            @PathVariable Long trackId,
            @RequestBody TrackUpdateRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(trackService.updateTrack(trackId, request))
        );
    }

    // 삭제
    @DeleteMapping("/{trackId}")
    public ResponseEntity<ApiResponse<String>> deleteTrack(@PathVariable Long trackId) {
        trackService.deleteTrack(trackId);
        return ResponseEntity.ok(ApiResponse.success("트랙이 삭제되었습니다."));
    }
}
