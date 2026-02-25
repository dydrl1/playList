package com.playlist.backend.playlist;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class PlaylistViewCountRepository {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String USER_VIEW_HISTORY_PREFIX = "playlist:view:history:"; // 유저별 방문 기록

    /**
     * 조회수 증가 (중복 방지 적용)
     * @return 증가 여부 (true: 처음 방문하여 증가함, false: 이미 방문함)
     */
    public boolean incrementIfFirstVisit(Long playlistId, Long userId) {
        // 비로그인 사용자는 일단 모두 증가시키거나, IP 기반 등으로 처리 가능 (여기서는 로그인 유저 기준)
        if (userId == null) {
            increment(playlistId);
            return true;
        }

        String historyKey = USER_VIEW_HISTORY_PREFIX + playlistId + ":" + userId;

        // 1. 해당 유저가 이 플리를 본 적이 있는지 확인 (값이 없으면 세팅하고 true 반환)
        // setIfAbsent는 값이 없을 때만 저장하며, 24시간 후 자동 삭제되도록 설정
        Boolean isFirstVisit = redisTemplate.opsForValue()
                .setIfAbsent(historyKey, "V", Duration.ofHours(24));

        if (Boolean.TRUE.equals(isFirstVisit)) {
            increment(playlistId);
            return true;
        }
        return false;
    }

    // 키 형식을 여기서만 관리
    private static final String KEY_PREFIX = "playlist:view:count:";

    public void increment(Long playlistId) {
        redisTemplate.opsForValue().increment(KEY_PREFIX + playlistId);
    }

    public long getCount(Long playlistId) {
        String value = redisTemplate.opsForValue().get(KEY_PREFIX + playlistId);
        return (value != null) ? Long.parseLong(value) : 0L;
    }

    // 스케줄러에서 사용할 모든 키 조회 및 삭제 로직도 여기에 포함
    public Set<String> findAllKeys() {
        return redisTemplate.keys(KEY_PREFIX + "*");
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }
}
