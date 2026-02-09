import { useState, useEffect, useRef } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";
import { Link } from "react-router-dom";
import api from "@/api/axios";

interface Track {
  title: string;
  artist: string;
  imageUrl: string;
  tumbnail: string
  }

interface Playlist {
  id: number;
  title: string;
  ownerName: string;
  thumbnailUrl?: string;
  trackCount: number;
  viewCount: number;
}

export default function Home() {
  const [query, setQuery] = useState("");
  const [results, setResults] = useState<Track[]>([]);
  const [myPlaylists, setMyPlaylists] = useState([]);
  const [publicPlaylists, setPublicPlaylists] = useState<Playlist[]>([]);
  const [page, setPage] = useState(1);
  const containerRef = useRef<HTMLDivElement>(null);
  const [isSearching, setIsSearching] = useState(false);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  // API 호출
    const fetchResults = async (q: string, p: number, append = false) => {
      if (!q.trim()) return;

      setLoading(true);
      try {
        const res = await axios.get(`/integraions/search`, {
          params: { query: q, page: p },
        });
        const data: Track[] = res.data.tracks;
        setResults((prev) => (append ? [...prev, ...data] : data));
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    };

  const fetchPublicPlaylists = async () => {
      try {
        setLoading(true);
        const res = await api.get("/api/playlists", {
          params: { sort: "LATEST", size: 10 }
        });
        // Page 객체 구조에 따라 res.data.data.content 또는 res.data.content 확인 필요
        const list = res.data.data?.content || res.data.content || [];
        setPublicPlaylists(list);
      } catch (err) {
        console.error("공개 플레이리스트 로딩 실패:", err);
      } finally {
        setLoading(false);
      }
    };


  // 로그인 즉시 상단문구 변경
  const [isLoggedIn, setIsLoggedIn] = useState(
    !!localStorage.getItem("accessToken")
  );

  useEffect(() => {
    const syncAuth = () =>
      setIsLoggedIn(!!localStorage.getItem("accessToken"));

    window.addEventListener("auth-change", syncAuth);
    return () => window.removeEventListener("auth-change", syncAuth);
  }, []);


  // 검색 엔터
  const handleSearch = async (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === "Enter" && query.trim() !== "") {
      try {
        const provider = "YOUTUBE"; //  ProviderType Enum 값
        const limit = 15;            // 원하는 검색 결과 개수

        const res = await fetch(
          `http://localhost:8080/integrations/search?provider=${provider}&query=${encodeURIComponent(query)}&limit=${limit}`
        );
        if (!res.ok) throw new Error("검색 API 호출 실패");

        const data: ExternalTrackDto[] = await res.json();
        setResults(data);
        setPage(1);
        setIsSearching(true); // 검색 상태 유지
      } catch (err) {
        console.error("검색 실패:", err);
      }
    }
  };


// 1. handleAddTrack 함수를 여기에 추가하세요!
  const handleAddTrack = async (track: any) => {
      if (!selectedPlaylistId) {
        alert("먼저 곡을 추가할 플레이리스트를 선택해주세요!");
        return;
      }

      try {
        // 백엔드 PlaylistTracksAddRequest DTO 구조에 맞춰 데이터 구성
        const requestData = {
          trackIds: [track.id], // 백엔드가 리스트(List) 형태를 받는지 확인 필요
          // 만약 곡 정보를 직접 받는 DTO라면 아래와 같이 구성
          title: track.title,
          artist: track.artist,
          imageUrl: track.imageUrl,
          spotifyTrackId: track.id
        };

        await api.post(`/api/playlists/${selectedPlaylistId}/tracks`, requestData);
        alert(`'${track.title}' 곡이 플레이리스트에 추가되었습니다! ✨`);
      } catch (err: any) {
        console.error("추가 실패:", err);
        alert(err.response?.data?.message || "곡 추가에 실패했습니다.");
      }
    };


  // 무한 스크롤
  useEffect(() => {
    const handleScroll = () => {
      if (!containerRef.current) return;
      const { scrollTop, scrollHeight, clientHeight } = containerRef.current;
      if (scrollTop + clientHeight >= scrollHeight - 100) {
        setPage((prev) => prev + 1);
        setResults((prev) => [...prev, ...generateResults(query, 10)]);
      }
    };
    const container = containerRef.current;
    container?.addEventListener("scroll", handleScroll);
    return () => container?.removeEventListener("scroll", handleScroll);
  }, [query]);

  return (
    <div className="relative min-h-screen w-full overflow-hidden">
      {/* 배경 */}
      <div className="absolute inset-0 bg-gradient-to-br from-gray-200 via-gray-300 to-gray-400 blur-3xl scale-110" />

      {/* Content */}
      <div
        ref={containerRef}
        className="relative z-10 flex flex-col items-center pt-20 px-10 max-h-screen overflow-auto"
      >
      {/* 로그인 버튼 */}
      <div className="absolute top-6 right-8 z-20 flex gap-4">
        {!isLoggedIn ? (
          <Link
            to="/login"
            className="
              text-lg font-semibold
              text-gray-700
              hover:text-black
              transition-colors
            "
          >
            로그인
          </Link>
        ) : (
          <>
            <button
              onClick={() => {
                localStorage.removeItem("accessToken");
                window.dispatchEvent(new Event("auth-change"));
                navigate("/mypage");
              }}
              className="
                text-lg font-semibold
                text-gray-700
                hover:text-black
                transition-colors
              "
            >
              로그아웃
            </button>

            <Link
              to="/mypage"
              className="
                text-lg font-semibold
                text-gray-700
                hover:text-black
                transition-colors
              "
            >
              마이페이지
            </Link>
          </>
        )}
      </div>

        {/* 검색/로고 컨테이너 */}
        <div
          className={`
            w-full max-w-[1200px] flex transition-all duration-500 ease-in-out
            ${isSearching ? "flex-row justify-start items-center gap-4 mt-16" : "flex-col justify-center items-center gap-6 mt-0"}
          `}
        >
          {/* 로고 */}
          <img
            src="/src/assets/logo.png"
            alt="Playlog Logo"
            className={`
              transition-all duration-500 ease-in-out
              ${isSearching ? "w-48" : "w-72"}
            `}
          />
          {/* 검색창 */}
          <input
            type="text"
            placeholder="플레이리스트를 검색해보세요"
            value={query}
            onChange={(e) => {
              const value = e.target.value; // value 정의
              setQuery(value);

              // 검색 여부 상태
              setIsSearching(value.trim() !== "");

              // 검색어가 비워지면 카드 초기화
              if (value.trim() === "") {
                setResults([]);
                setPage(1); // 페이지 초기화
              }
            }}
            onKeyDown={handleSearch}
            className={`
              transition-all duration-500 ease-in-out
              ${isSearching ? "w-full max-w-[600px] mt-0" : "w-full max-w-[1200px] mt-10"}
              px-6 py-4 rounded-full bg-white/80 backdrop-blur-md shadow-lg focus:outline-none focus:ring-2 focus:ring-gray-700 placeholder:text-gray-400 text-lg
            `}
          />
        </div>

        {/* 메인 결과 섹션 */}
        <div className="w-full max-w-[1200px] mt-12 mb-20">
          {isSearching ? (
            /* -----------------------------------------------------------
               [케이스 1] 검색 중일 때: 유튜브 곡 검색 결과(results) 노출
               ----------------------------------------------------------- */
            <>
              <h3 className="text-xl font-bold text-gray-800 mb-6 px-2">곡 검색 결과</h3>
              <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-5 gap-4">
                {results.map((item, idx) => (
                  <div
                    key={idx}
                    className="group relative flex flex-col bg-white/90 rounded-xl shadow-md overflow-hidden cursor-pointer animate-fade-in hover:shadow-2xl hover:-translate-y-1 transition-all duration-300"
                  >
                    {/* 이미지 섹션 */}
                    <div className="relative h-48 overflow-hidden">
                      <img
                        src={item.imageUrl || item.tumbnail}
                        alt={item.title}
                        className="w-full h-full object-cover transition-transform duration-500 group-hover:scale-110"
                      />
                      {/* 오버레이 버튼 (추가) */}
                      <div className="absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity duration-300 flex items-center justify-center">
                        <button
                          onClick={(e) => { e.stopPropagation(); handleAddTrack(item); }}
                          className="p-3 bg-gray-900 rounded-full hover:bg-black text-white shadow-lg transform hover:scale-110 transition-all active:scale-95"
                        >
                          <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 4v16m8-8H4" />
                          </svg>
                        </button>
                      </div>
                    </div>
                    {/* 텍스트 정보 */}
                    <div className="p-3 flex flex-col gap-1 bg-white">
                      <span className="font-bold text-gray-800 text-sm truncate">{item.title}</span>
                      <span className="text-gray-500 text-xs truncate">{item.artist}</span>
                    </div>
                  </div>
                ))}
              </div>
            </>
          ) : (
            /* -----------------------------------------------------------
               [케이스 2] 검색 전일 때: 공개 플레이리스트(publicPlaylists) 노출
               ----------------------------------------------------------- */
            <>
              <div className="flex justify-between items-end mb-8 px-2">
                <div>
                  <h3 className="text-3xl font-black text-gray-900">둘러보기 🔥</h3>
                  <p className="text-gray-500 mt-1 font-medium">다른 유저들의 플레이리스트를 감상해보세요</p>
                </div>
              </div>

              <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-5 gap-6">
                {publicPlaylists.map((pl) => (
                  <div
                    key={pl.id}
                    onClick={() => navigate(`/playlists/${pl.id}`)}
                    className="group relative flex flex-col bg-white/70 backdrop-blur-md rounded-[2.5rem] shadow-sm border border-white/50 overflow-hidden cursor-pointer hover:shadow-2xl hover:-translate-y-2 transition-all duration-500 p-4"
                  >
                    {/* 플레이리스트 썸네일 */}
                    <div className="relative aspect-square rounded-[2rem] overflow-hidden bg-gray-200 mb-4 shadow-inner">
                      {pl.thumbnailUrl ? (
                        <img src={pl.thumbnailUrl} className="w-full h-full object-cover group-hover:scale-110 transition-transform duration-700" alt={pl.title} />
                      ) : (
                        <div className="w-full h-full flex items-center justify-center bg-gradient-to-br from-gray-100 to-gray-300 text-5xl">
                          🎧
                        </div>
                      )}
                    </div>
                    {/* 플레이리스트 정보 */}
                    <div className="px-2 pb-2">
                      <h4 className="font-black text-gray-900 text-lg truncate mb-1 group-hover:text-gray-600 transition-colors">
                        {pl.title}
                      </h4>
                      <span className="text-gray-500 text-xs font-bold">by {pl.ownerName}</span>
                      <div className="flex justify-between items-center mt-3">
                        <span className="text-[10px] bg-black/5 px-2 py-1 rounded-full text-gray-600 font-bold">곡 {pl.trackCount || 0}개</span>
                        <span className="text-[10px] text-gray-400 font-medium">조회 {pl.viewCount}</span>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  );
}
