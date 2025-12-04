package com.playlist.backend.playlist;

import com.playlist.backend.common.exception.BusinessException;
import com.playlist.backend.common.exception.ErrorCode;
import com.playlist.backend.playlist.dto.PlaylistCreateRequest;
import com.playlist.backend.playlist.dto.PlaylistUpdateRequest;
import com.playlist.backend.user.User;
import com.playlist.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.playlist.backend.playlist.dto.PlaylistResponse;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final UserRepository userRepository;

    // 내 플레이리스트 전체 조회
    public List<PlaylistResponse> getMyPlaylists(Long userId){
        return playlistRepository.findAllByUserId(userId).stream()
                .map(PlaylistResponse::from)
                .toList();
    }


    // 내 플레이리스트 상세 조회
    public PlaylistResponse getMyPlaylist(Long userId, Long playlistId){
        Playlist playlist = playlistRepository.findByIdAndUserId(playlistId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PLAYLIST_NOT_FOUND));

        return PlaylistResponse.from(playlist);
    }

    //플레이리스트 생성
    public PlaylistResponse createPlaylist(Long userId, PlaylistCreateRequest request){
        User ower = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        boolean isPublic = request.getIsPublic() == null || request.getIsPublic();

        Playlist playlist = Playlist.builder()
                .user(ower)
                .title(request.getTitle())
                .description(request.getDescription())
                .isPublic(isPublic)
                .build();

        Playlist saved = playlistRepository.save(playlist);

        return PlaylistResponse.from(saved);
    }


    // 플레이리스트 수정
    public PlaylistResponse updateMyPlaylist(Long userId, Long playlistId, PlaylistUpdateRequest request){
        Playlist playlist = playlistRepository.findByIdAndUserId(playlistId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PLAYLIST_NOT_FOUND));

        playlist.update(
                request.getTitle(),
                request.getDescription(),
                request.getIsPublic()
        );

        return PlaylistResponse.from(playlist);
    }


    // 플레이리스트 삭제
    public void deleteMyPlaylist(Long userId, Long playlistId){
        Playlist playlist =playlistRepository.findByIdAndUserId(playlistId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PLAYLIST_NOT_FOUND));

        playlistRepository.delete(playlist);
    }
}
