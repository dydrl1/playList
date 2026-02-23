package com.playlist.backend.Track;

import com.playlist.backend.Track.dto.TrackCreateRequest;
import com.playlist.backend.Track.dto.TrackPlayResponse;
import com.playlist.backend.Track.dto.TrackResponse;
import com.playlist.backend.Track.dto.TrackUpdateRequest;
import com.playlist.backend.common.exception.BusinessException;
import com.playlist.backend.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TrackService {

    private final TrackRepository trackRepository;


    /**
     * =========================================
     *  트랙 단건 조회 (상세 조회)
     * =========================================
     * - 트랙 ID(PK) 기반 조회
     * - 존재하지 않을 경우 TRACK_NOT_FOUND 발생
     */
    public TrackResponse getTrack(Long trackId){
        Track track = trackRepository.findById(trackId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRACK_NOT_FOUND));
        return TrackResponse.from(track);
    }


    /**
     * =========================================
     *  트랙 목록 조회 / 검색
     * =========================================
     * - keyword가 없으면 전체 트랙 목록 조회
     * - keyword가 있으면 제목/아티스트 기준 검색
     * - 페이징(Pageable) 및 정렬 지원
     */
    public Page<TrackResponse> getTracks(String keyword, Pageable pageable) {

        // 검색어 길이 정책 검증
        if (keyword != null && !keyword.isBlank() && keyword.length() < 2) {
            throw new BusinessException(ErrorCode.TRACK_SEARCH_KEYWORD_TOO_SHORT);
        }

        Page<Track> page;

        if (keyword == null || keyword.isBlank()) {
            page = trackRepository.findAll(pageable);
        } else {
            page = trackRepository
                    .findByTitleContainingIgnoreCaseOrArtistContainingIgnoreCase(
                            keyword, keyword, pageable
                    );
        }

        return page.map(TrackResponse::from);
    }

    /**
     * =========================================
     *  트랙 생성
     * =========================================
     * - 필수 값(title, artist) 검증
     * - 새로운 트랙 엔티티 생성 및 저장
     */
    @Transactional
    public TrackResponse createTrack(TrackCreateRequest request) {
    if (request.getTitle() == null || request.getTitle().isBlank()) {
        throw new IllegalStateException("title은 필수입니다.");
    }
    if (request.getArtist() == null || request.getArtist().isBlank()) {
        throw new IllegalStateException(("artist는 필수입니다."));
    }

        Track track = new Track(request.getTitle(), request.getArtist(), request.getImageUrl());
    track.setAlbum(request.getAlbum());
    track.setDurationSec(request.getDurationSec());
    track.setSourceType(request.getSourceType());
    track.setSourceUrl(request.getSourceUrl());
    track.setImageUrl(request.getImageUrl());

    Track saved = trackRepository.save(track); // INSERT
    return TrackResponse.from(saved);
    }


    /**
     * =========================================
     *  트랙 수정 (부분 수정 / PATCH 스타일)
     * =========================================
     * - 트랙 ID 기준 조회
     * - 요청 값 중 null이 아닌 필드만 반영
     * - 더티 체킹으로 UPDATE 수행
     */
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
        if (request.getImageUrl() != null) track.setImageUrl(request.getImageUrl());

        // save() 없어도 더티체킹으로 UPDATE 되지만, 명시적으로 해도 됨
        return TrackResponse.from(track);
    }

    /**
     * =========================================
     *  트랙 삭제
     * =========================================
     * - 트랙 ID 기준 조회
     * - 존재하지 않을 경우 TRACK_NOT_FOUND 발생
     * - 트랙 엔티티 삭제
     */
    @Transactional
    public void deleteTrack(Long trackId) {
        Track track = trackRepository.findById(trackId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRACK_NOT_FOUND));
        trackRepository.delete(track); // DELETE
    }


    /**
     * 곡 재생 정보 조회
     */
    @Transactional(readOnly = true)
    public TrackPlayResponse getTrackPlayInfo(Long trackId) {
        // 1. 곡 존재 여부 확인
        Track track = trackRepository.findById(trackId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRACK_NOT_FOUND));

        // 2. Null 방어 로직 추가
        return TrackPlayResponse.builder()
                .trackId(track.getId())
                .title(track.getTitle())
                .artist(track.getArtist())
                // sourceType이 null이면 "UNKNOWN" 혹은 기본값 세팅
                .sourceType(track.getSourceType() != null ? track.getSourceType() : "YOUTUBE")
                .sourceUrl(track.getSourceUrl())
                // imageUrl이 null이면 빈 문자열로 대체하여 NPE 방지
                .imageUrl(track.getImageUrl() != null ? track.getImageUrl() : "")
                .build();
    }
}
