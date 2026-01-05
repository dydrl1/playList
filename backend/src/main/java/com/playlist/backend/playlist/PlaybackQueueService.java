package com.playlist.backend.playlist;

import com.playlist.backend.common.exception.BusinessException;
import com.playlist.backend.common.exception.ErrorCode;
import com.playlist.backend.playlist.dto.PlaybackQueueResponse;
import com.playlist.backend.playlistTrack.PlaylistTrack;
import com.playlist.backend.playlistTrack.PlaylistTrackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlaybackQueueService {

    private final PlaylistRepository playlistRepository;
    private final PlaylistTrackRepository playlistTrackRepository;

    @Transactional(readOnly = true)
    public PlaybackQueueResponse getQueue(Long playlistId, Long loginUserId) {

        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PLAYLIST_NOT_FOUND));

        // 비공개 플레이리스트 접근제어 : 소유자만 조회 가능
        if (!playlist.isPublic() && (loginUserId == null || !playlist.getUser().getId().equals(loginUserId))) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
            List<PlaylistTrack> pts = playlistTrackRepository.findQueueByPlaylistId(playlistId);

            List<PlaybackQueueResponse.QueueItem> items = pts.stream()
                    .map(PlaybackQueueResponse.QueueItem::from)
                    .toList();

            return PlaybackQueueResponse.of(playlistId, items);
    }
}


