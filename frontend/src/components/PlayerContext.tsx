import React, { createContext, useContext, useState, useEffect } from "react";

interface Track {
  trackId: number;
  title: string;
  artist: string;
  sourceUrl: string;
  imageUrl: string;
}

interface PlayerContextType {
  currentTrack: Track | null;
  isPlaying: boolean;
  playTrack: (track: Track) => void;
  pauseTrack: () => void;
}

const PlayerContext = createContext<PlayerContextType | undefined>(undefined);

export const PlayerProvider = ({ children }: { children: React.ReactNode }) => {
  const [currentTrack, setCurrentTrack] = useState<Track | null>(null);
  const [isPlaying, setIsPlaying] = useState(false);

  // 로그아웃 감지 (토큰이 없어지면 재생 중지)
  useEffect(() => {
    const token = localStorage.getItem("accessToken");
    if (!token) {
      setCurrentTrack(null);
      setIsPlaying(false);
    }
  }, [window.location.pathname]); // 페이지 이동 시마다 체크하거나 토큰 상태를 구독

  const playTrack = (track: Track) => {
    setCurrentTrack(track);
    setIsPlaying(true);
  };

  const pauseTrack = () => setIsPlaying(false);

  return (
    <PlayerContext.Provider value={{ currentTrack, isPlaying, playTrack, pauseTrack }}>
      {children}
    </PlayerContext.Provider>
  );
};

export const usePlayer = () => {
  const context = useContext(PlayerContext);
  if (!context) throw new Error("usePlayer must be used within PlayerProvider");
  return context;
};