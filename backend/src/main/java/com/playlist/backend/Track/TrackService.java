package com.playlist.backend.Track;

import com.playlist.backend.Track.dto.TrackCreateRequest;
import com.playlist.backend.Track.dto.TrackResponse;
import com.playlist.backend.Track.dto.TrackUpdateRequest;
import com.playlist.backend.common.exception.BusinessException;
import com.playlist.backend.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TrackService {

    private final TrackRepository trackRepository;

    public TrackResponse getTrack(Long trackId){
        Track track = trackRepository.findById(trackId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRACK_NOT_FOUND));
        return TrackResponse.from(track);
    }

    public List<TrackResponse> getTracks(String keyword){
        List<Track> tracks;
        if (keyword == null || keyword.isBlank()){
            tracks = trackRepository.findAll();
        }else {
            tracks = trackRepository
                    .findByTitleContainingIgnoreCaseOrArtistContainingIgnoreCase(keyword, keyword);
        }
        return tracks.stream()
                .map(TrackResponse::from)
                .toList();
    }

@Transactional
public TrackResponse createTrack(TrackCreateRequest request) {
    if (request.getTitle() == null || request.getTitle().isBlank()) {
        throw new IllegalStateException("title은 필수입니다.");
    }
    if (request.getArtist() == null || request.getArtist().isBlank()) {
        throw new IllegalStateException(("artist는 필수입니다."));
    }

    Track track = new Track(request.getTitle(), request.getArtist());
    track.setAlbum(request.getAlbum());
    track.setDurationSec(request.getDurationSec());
    track.setSourceType(request.getSourceType());
    track.setSourceUrl(request.getSourceUrl());

    Track saved = trackRepository.save(track); // INSERT
    return TrackResponse.from(saved);
}

    @Transactional
    public TrackResponse updateTrack(Long trackId, TrackUpdateRequest request) {
        Track track = trackRepository.findById(trackId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRACK_NOT_FOUND));

        // 부분 수정(PATCH 스타일): null이 아니면 반영
        if (request.getTitle() != null) track.setTitle(request.getTitle());
        if (request.getArtist() != null) track.setArtist(request.getArtist());
        if (request.getAlbum() != null) track.setAlbum(request.getAlbum());
        if (request.getDurationSec() != null) track.setDurationSec(request.getDurationSec());
        if (request.getSourceType() != null) track.setSourceType(request.getSourceType());
        if (request.getSourceUrl() != null) track.setSourceUrl(request.getSourceUrl());

        // save() 없어도 더티체킹으로 UPDATE 되지만, 명시적으로 해도 됨
        return TrackResponse.from(track);
    }

    @Transactional
    public void deleteTrack(Long trackId) {
        Track track = trackRepository.findById(trackId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRACK_NOT_FOUND));
        trackRepository.delete(track); // DELETE
    }
}
