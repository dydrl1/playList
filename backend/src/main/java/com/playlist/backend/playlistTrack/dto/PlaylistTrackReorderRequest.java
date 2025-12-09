package com.playlist.backend.playlistTrack.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class PlaylistTrackReorderRequest {

    // 예 : [10, 5, 7] -> trackid 10이 1번, 5가 2번, 7이 3번
    private List<Long> trackIds;
}
