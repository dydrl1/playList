import ReactPlayer from "react-player";
import { usePlayer } from "./PlayerContext";
import { useState, useRef } from "react";

export default function PlayerBar() {
  const { currentTrack, isPlaying, togglePlay } = usePlayer();
  const playerRef = useRef<ReactPlayer>(null); // 재생 위치 조절을 위한 Ref

  // --- 상태 관리 ---
  const [played, setPlayed] = useState(0); // 재생 진행률 (0 ~ 1)
  const [duration, setDuration] = useState(0); // 전체 길이(초)
  const [seeking, setSeeking] = useState(false); // 드래그 중인지 여부
  const [volume, setVolume] = useState(0.5); // 볼륨 (0 ~ 1)

  const defaultImage = "https://via.placeholder.com/150?text=No+Image";

  if (!currentTrack) return null;

  // 유튜브 ID를 재생 가능한 풀 주소로 변환
  // const videoUrl = `https://www.youtube.com/watch?v=${currentTrack.sourceUrl}`;
  const videoUrl = `https://www.youtube.com/watch?v=dQw4w9WgXcQ`;
  console.log("실제 호출 주소:", videoUrl);

  // --- 이벤트 핸들러 ---
  const handleSeekChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setPlayed(parseFloat(e.target.value));
  };

  const handleSeekMouseUp = (e: React.MouseEvent<HTMLInputElement> | React.TouchEvent<HTMLInputElement>) => {
    setSeeking(false);

    // 2. 값 읽어오기 (e.currentTarget 활용)
    const seekToValue = parseFloat((e.target as HTMLInputElement).value);

    // 3. 존재 여부와 메서드 존재 여부를 동시에 체크
    if (playerRef.current) {
      playerRef.current.seekTo(seekToValue);
    }
  };

  const handleProgress = (state: { played: number }) => {
    if (!seeking) {
      setPlayed(state.played);
    }
  };

  // 시간 포맷 변환 (초 -> 00:00)
  const formatTime = (seconds: number) => {
    const date = new Date(seconds * 1000);
    const mm = date.getUTCMinutes();
    const ss = date.getUTCSeconds().toString().padStart(2, "0");
    return `${mm}:${ss}`;
  };


  return (
    <div className="fixed bottom-0 left-0 w-full bg-white/90 backdrop-blur-md border-t p-4 z-50 flex items-center justify-between px-8 shadow-lg">
      <div className="flex items-center gap-4 w-72">

        {/* 1. 곡 정보 */}
        <div className="flex items-center gap-4 w-1/4 min-w-[200px]">
          <img
            src={currentTrack.imageUrl || defaultImage}
            alt={currentTrack.title}
            className="w-12 h-12 rounded-lg object-cover shadow-sm"
          />
          <div className="truncate">
            <p className="font-bold truncate text-sm text-gray-800">{currentTrack.title}</p>
            <p className="text-xs text-gray-500 truncate">{currentTrack.artist}</p>
          </div>
        </div>

        {/* 2. 중앙 컨트롤 및 재생바 */}
          <div className="flex flex-col items-center flex-1 max-w-2xl gap-1">
            <div className="flex items-center gap-5">
              <button
                onClick={togglePlay} // 👈 복잡한 조건문 대신 Context의 togglePlay 직접 호출
                className="text-3xl hover:scale-110 transition-transform"
              >
                {isPlaying ? "⏸️" : "▶️"}
              </button>
            </div>

            {/* 재생바 UI */}
            <div className="w-full flex items-center gap-3">
              <span className="text-[10px] text-gray-400 w-8 text-right">{formatTime(duration * played)}</span>
              <input
                type="range"
                min={0}
                max={0.999999}
                step="any"
                value={played}
                onMouseDown={() => setSeeking(true)}
                onChange={handleSeekChange}
                onMouseUp={handleSeekMouseUp}
                className="flex-1 h-1 bg-gray-200 rounded-lg appearance-none cursor-pointer accent-black"
              />
              <span className="text-[10px] text-gray-400 w-8">{formatTime(duration)}</span>
            </div>
          </div>

          {/* 3. 우측 볼륨 조절 */}
          <div className="flex items-center gap-3 w-1/4 justify-end">
            <span className="text-xs">🔈</span>
            <input
              type="range"
              min={0}
              max={1}
              step="any"
              value={volume}
              onChange={(e) => setVolume(parseFloat(e.target.value))}
              className="w-20 h-1 bg-gray-200 rounded-lg appearance-none cursor-pointer accent-gray-600"
            />
            <span className="text-xs">🔊</span>
          </div>
        </div>

        {/* 실제 유튜브 플레이어 (숨김) */}
        <div className="fixed bottom-24 right-4 border shadow-xl bg-black overflow-hidden rounded-lg">
              <ReactPlayer
                ref={playerRef}
                // 2. 여기를 currentTrack.sourceUrl 대신 위에 만든 videoUrl로 변경하세요!
                url={videoUrl}
                playing={isPlaying}
                controls={true}
                volume={volume}
                width="320px" // 테스트를 위해 크기를 키워서 확인
                height="180px"
                onProgress={handleProgress}
                onDuration={(d) => setDuration(d)}
                onReady={() => console.log("✅ 플레이어 로드 성공!")}
                onError={(e) => console.log("❌ 재생 불가 에러:", e)}
                config={{
                  youtube: {
                    playerVars: {
                      origin: window.location.origin,
                      enablejsapi: 1
                    }
                  }
                }}
              />
        </div>
    </div>
  );
}