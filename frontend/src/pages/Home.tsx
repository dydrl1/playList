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
  ownerId: number; // 내 플리인지 확인
  thumbnailUrl?: string;
  trackCount: number;
  viewCount: number;
  isLiked: boolean;  // 내가 좋아요 눌렀는지 여부
  likeCount: number; // 총 좋아요 수
}

export default function Home() {
  const [query, setQuery] = useState("");
  const [results, setResults] = useState<Track[]>([]);
  const [myPlaylists, setMyPlaylists] = useState<Playlist[]>([]);
  const [selectedPlaylistId, setSelectedPlaylistId] = useState<number | null>(null);
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
        params: {
          sort: "LATEST", // LATEST, VIEW, LIKE 중 선택 가능
          page: 0,        // 백엔드 Pageable은 0부터 시작합니다
          size: 10
        }
      });

        // 1. 여기서 로그를 찍어서 데이터 구조를 반드시 확인하세요!
      console.log("전체 응답 객체:", res);
      console.log("실제 데이터 경로 확인:", res.data);

      const content = res.data.data?.content || [];
      setPublicPlaylists(content);
    } catch (err) {
      console.error("공개 플레이리스트 로딩 실패:", err);
    } finally {
      setLoading(false);
    }
  };

  // 2. 컴포넌트 마운트 시 호출
  useEffect(() => {
    fetchPublicPlaylists();
  }, []); // 빈 배열을 넣어 처음에 한 번만 실행되게 합니다.


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


// 1. handleAddTrack 함수 최종 정리
const handleAddTrack = async (track: any) => {
  // 로그 확인 (디버깅용)
  console.log("원본 트랙 데이터:", track);

  // 1. 선택된 플레이리스트가 있는지 확인
  if (!selectedPlaylistId) {
    alert("곡을 추가할 플레이리스트를 먼저 선택해주세요! 🎵");
    return;
  }

  // 2. 로그인 여부 확인 (혹시 모를 상황 대비)
  if (!isLoggedIn) {
    alert("로그인이 필요한 서비스입니다. 🔒");
    navigate("/login");
    return;
  }

  try {
    // 3. 백엔드 DTO(PlaylistTracksAddRequest) 구조에 맞게 데이터 구성
    const requestData = {
      tracks: [
        {
          title: track.title,
          artist: track.artist,
          album: "Single", // 백엔드에서 null을 허용하지 않을 수 있으므로 기본값 설정
          imageUrl: track.imageUrl || track.tumbnail,
          durationSec: track.durationSec || 0,
          sourceType: track.provider || "YOUTUBE", // 로그의 'YOUTUBE' 사용
          sourceUrl: track.externalId,           // 로그의 'externalId' 사용
          trackOrder: null                       // 마지막 순서로 추가
        }
      ]
    };

    console.log("보내는 데이터:", requestData);

    // 4. API 호출
    await api.post(`/api/playlists/${selectedPlaylistId}/tracks`, requestData);

    alert(`'${track.title}' 곡이 플레이리스트에 추가되었습니다! ✨`);

  } catch (err: any) {
    console.error("추가 실패 상세:", err.response?.data);

    // 중복 추가 등 백엔드에서 보낸 구체적인 에러 메시지 표시
    const serverMessage = err.response?.data?.message;
    alert(serverMessage || "곡 추가 중 오류가 발생했습니다.");
  }
};

  // 내 플레이리스트 목록 가져오기
  useEffect(() => {
    fetchPublicPlaylists();

    // 로그인 상태라면 내 플레이리스트도 가져오기
    if (isLoggedIn) {
      api.get("/api/me/playlists").then(res => {
        setMyPlaylists(res.data.data || []);
        // 첫 번째 플레이리스트를 기본값으로 설정 (선택 사항)
        if (res.data.data?.length > 0) setSelectedPlaylistId(res.data.data[0].id);
      });
    }
  }, [isLoggedIn]);


  // 무한 스크롤 최적화 및 API 연결
  useEffect(() => {
    const handleScroll = () => {
      if (!containerRef.current || loading || !isSearching) return;

      const { scrollTop, scrollHeight, clientHeight } = containerRef.current;

      // 바닥에서 100px 위에 도달했을 때 실행
      if (scrollTop + clientHeight >= scrollHeight - 100) {
        // 다음 페이지 데이터 호출
        const nextPage = page + 1;
        setPage(nextPage);

        // 실제 API를 호출하여 결과를 추가(append: true)
        fetchResults(query, nextPage, true);
      }
    };

    // 쓰로틀링(Throttling)을 적용하면 성능이 더 좋아지지만, 우선 기본 이벤트로 등록
    const container = containerRef.current;
    container?.addEventListener("scroll", handleScroll);

    return () => container?.removeEventListener("scroll", handleScroll);
  }, [query, page, loading, isSearching]);


// 좋아요 토글 함수
const handleLikeToggle = async (e: React.MouseEvent, playlist: Playlist) => {
  e.stopPropagation(); // 카드 클릭(상세페이지 이동) 이벤트 방지

  if (!isLoggedIn) {
    alert("좋아요를 누르려면 로그인이 필요합니다. 🔒");
    navigate("/login");
    return;
  }

  // 내 플레이리스트인지 체크 (백엔드에서 받아온 ownerId와 내 ID 비교)
  // 로그인을 관리하는 방식에 따라 다를 수 있지만, 보통 localStorage나 Context에 저장된 ID를 사용합니다.
  const myId = localStorage.getItem("userId");
  if (myId && Number(myId) === playlist.ownerId) {
    alert("본인의 플레이리스트에는 좋아요를 누를 수 없습니다. 😊");
    return;
  }

  // [낙관적 업데이트] 서버 응답 전에 화면 UI를 먼저 변경해서 사용자 경험을 높임
  const originalPlaylists = [...publicPlaylists];
  setPublicPlaylists((prev) =>
    prev.map((pl) =>
      pl.id === playlist.id
        ? {
            ...pl,
            isLiked: !pl.isLiked,
            likeCount: pl.isLiked ? pl.likeCount - 1 : pl.likeCount + 1,
          }
        : pl
    )
  );

  try {
    if (playlist.isLiked) {
      // 이미 좋아요 상태면 취소 (Delete)
      await api.delete(`/api/playlists/${playlist.id}/unlikes`);
    } else {
      // 좋아요 추가 (Post)
      await api.post(`/api/playlists/${playlist.id}/likes`);
    }
  } catch (err) {
    console.error("좋아요 처리 실패:", err);
    // 에러 발생 시 이전 상태로 롤백
    setPublicPlaylists(originalPlaylists);
    alert("처리 중 오류가 발생했습니다. 다시 시도해주세요.");
  }
};


  return (
      <div className="relative min-h-screen w-full overflow-hidden">
        {/* 배경 */}
        <div className="absolute inset-0 bg-gradient-to-br from-gray-200 via-gray-300 to-gray-400 blur-3xl scale-110" />

        {/* Content */}
        <div
          ref={containerRef}
          className="relative z-10 flex flex-col items-center pt-20 px-10 max-h-screen overflow-auto"
        >
          {/* 상단 버튼 (로그인/로그아웃/마이페이지) */}
          <div className="absolute top-6 right-8 z-20 flex gap-4">
            {!isLoggedIn ? (
              <Link to="/login" className="text-lg font-semibold text-gray-700 hover:text-black transition-colors">
                로그인
              </Link>
            ) : (
              <>
                <button
                  onClick={() => {
                    localStorage.removeItem("accessToken");
                    window.location.href = "/";
                  }}
                  className="text-lg font-semibold text-gray-700 hover:text-black transition-colors"
                >
                  로그아웃
                </button>
                <Link to="/mypage" className="text-lg font-semibold text-gray-700 hover:text-black transition-colors">
                  마이페이지
                </Link>
              </>
            )}
          </div>

          {/* 검색/로고 컨테이너 */}
          <div className={`w-full max-w-[1200px] flex transition-all duration-500 ease-in-out ${isSearching ? "flex-row justify-start items-center gap-4 mt-16" : "flex-col justify-center items-center gap-6 mt-0"}`}>
            <img
              src="/src/assets/logo.png"
              alt="Playlog Logo"
              className={`transition-all duration-500 ease-in-out ${isSearching ? "w-48" : "w-72"}`}
            />
            <input
              type="text"
              placeholder="플레이리스트를 검색해보세요"
              value={query}
              onChange={(e) => {
                const value = e.target.value;
                setQuery(value);
                setIsSearching(value.trim() !== "");
                if (value.trim() === "") {
                  setResults([]);
                  setPage(1);
                }
              }}
              onKeyDown={handleSearch}
              className={`transition-all duration-500 ease-in-out ${isSearching ? "w-full max-w-[600px] mt-0" : "w-full max-w-[1200px] mt-10"} px-6 py-4 rounded-full bg-white/80 backdrop-blur-md shadow-lg focus:outline-none focus:ring-2 focus:ring-gray-700 placeholder:text-gray-400 text-lg`}
            />
          </div>

          {/* 메인 결과 섹션 */}
          <div className="w-full max-w-[1200px] mt-12 mb-20">
            {isSearching ? (
              /* [케이스 1] 검색 결과 */
              <div className="flex flex-col">
                {/* 상단 헤더 영역: 제목과 플레이리스트 선택창을 한 줄에 배치 */}
                <div className="flex justify-between items-center mb-8 px-2">
                  <h3 className="text-2xl font-black text-gray-900">곡 검색 결과 🔍</h3>

                  {isLoggedIn && myPlaylists.length > 0 && (
                    <div className="flex items-center gap-3 bg-white/60 p-2 px-4 rounded-2xl backdrop-blur-md border border-white/50 shadow-sm">
                      <span className="text-[10px] font-black text-gray-400 uppercase tracking-widest">Add to</span>
                      <select
                        value={selectedPlaylistId || ""}
                        onChange={(e) => setSelectedPlaylistId(Number(e.target.value))}
                        className="bg-transparent border-none text-sm font-bold text-gray-800 focus:ring-0 cursor-pointer outline-none"
                      >
                        <option value="" disabled>플레이리스트 선택</option>
                        {myPlaylists.map(pl => (
                          <option key={pl.id} value={pl.id}>{pl.title}</option>
                        ))}
                      </select>
                    </div>
                  )}
                </div>

                {/* 검색 결과 그리드 */}
                <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-5 gap-6">
                  {results.map((item, idx) => (
                    <div
                      key={idx}
                      className="group relative flex flex-col bg-white/70 backdrop-blur-md rounded-[2rem] shadow-sm border border-white/50 overflow-hidden cursor-pointer hover:shadow-2xl hover:-translate-y-2 transition-all duration-500 p-3"
                    >
                      <div className="relative aspect-square rounded-[1.5rem] overflow-hidden bg-gray-200 shadow-inner">
                        <img
                          src={item.imageUrl || item.tumbnail}
                          alt={item.title}
                          className="w-full h-full object-cover transition-transform duration-700 group-hover:scale-110"
                        />
                        <div className="absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity duration-300 flex items-center justify-center">
                          <button
                            onClick={(e) => { e.stopPropagation(); handleAddTrack(item); }}
                            className="p-4 bg-white text-black rounded-full shadow-xl hover:scale-110 active:scale-90 transition-all"
                          >
                            <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24" strokeWidth="3">
                              <path strokeLinecap="round" strokeLinejoin="round" d="M12 4.5v15m7.5-7.5h-15" />
                            </svg>
                          </button>
                        </div>
                      </div>
                      <div className="mt-4 px-2 pb-2 flex flex-col gap-1">
                        <span className="font-black text-gray-900 text-base truncate">{item.title}</span>
                        <span className="text-gray-500 text-xs font-bold">{item.artist}</span>
                      </div>
                    </div>
                  ))}
                </div>

                {/* 결과가 없을 때 */}
                {results.length === 0 && !loading && (
                  <div className="text-center py-20 text-gray-400 font-medium">
                    검색 결과가 없습니다. 다른 키워드로 검색해보세요!
                  </div>
                )}
              </div>
            ) : (
              /* [케이스 2] 둘러보기 */
              <>
                <div className="flex justify-between items-end mb-8 px-2">
                  <div>
                    <h3 className="text-3xl font-black text-gray-900">둘러보기 🔥</h3>
                    <p className="text-gray-500 mt-1 font-medium">다른 유저들의 플레이리스트를 감상해보세요</p>
                  </div>
                </div>

                {publicPlaylists && publicPlaylists.length > 0 ? (
                  <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-5 gap-6">
                    {publicPlaylists.map((pl) => (
                      <div
                        key={pl.id}
                        onClick={() => navigate(`/playlists/${pl.id}`)}
                        className="group relative flex flex-col bg-white/70 backdrop-blur-md rounded-[2.5rem] shadow-sm border border-white/50 overflow-hidden cursor-pointer hover:shadow-2xl hover:-translate-y-2 transition-all duration-500 p-4"
                      >
                        <div className="relative aspect-square rounded-[2rem] overflow-hidden bg-gray-200 mb-4 shadow-inner">
                          {pl.thumbnailUrl ? (
                            <img
                              src={pl.thumbnailUrl}
                              className="w-full h-full object-cover group-hover:scale-110 transition-transform duration-700"
                              alt={pl.title}
                            />
                          ) : (
                            <div className="w-full h-full flex items-center justify-center bg-gradient-to-br from-gray-100 to-gray-300 text-5xl">
                              🎧
                            </div>
                          )}

                          {/* 이미지 우측 상단 하트 버튼 추가 */}
                          <button
                            onClick={(e) => handleLikeToggle(e, pl)}
                            className={`absolute top-4 right-4 p-2 rounded-full transition-all duration-300 backdrop-blur-md shadow-sm active:scale-90 ${
                              pl.isLiked
                                ? "bg-red-500 text-white fill-current"
                                : "bg-white/60 text-gray-600 hover:bg-white"
                            }`}
                          >
                            <svg
                              xmlns="http://www.w3.org/2000/svg"
                              fill={pl.isLiked ? "currentColor" : "none"}
                              viewBox="0 0 24 24"
                              strokeWidth={2}
                              stroke="currentColor"
                              className="w-5 h-5"
                            >
                              <path
                                strokeLinecap="round"
                                strokeLinejoin="round"
                                d="M21 8.25c0-2.485-2.099-4.5-4.688-4.5-1.935 0-3.597 1.126-4.312 2.733-.715-1.607-2.377-2.733-4.313-2.733C5.1 3.75 3 5.765 3 8.25c0 7.22 9 12 9 12s9-4.78 9-12Z"
                              />
                            </svg>
                          </button>
                        </div>

                        <div className="px-2 pb-2">
                          <h4 className="font-black text-gray-900 text-lg truncate mb-1 group-hover:text-gray-600 transition-colors">
                            {pl.title}
                          </h4>
                          <div className="flex justify-between items-center">
                            <span className="text-gray-500 text-xs font-bold truncate">by {pl.ownerName}</span>
                            {/* 좋아요 수 표시 */}
                            {pl.likeCount > 0 && (
                              <span className="text-[11px] text-red-500 font-bold flex items-center gap-1">
                                ❤️ {pl.likeCount}
                              </span>
                            )}
                          </div>

                          <div className="flex justify-between items-center mt-3">
                            <span className="text-[10px] bg-black/5 px-2 py-1 rounded-full text-gray-600 font-bold">
                              곡 {pl.trackCount || 0}개
                            </span>
                            <span className="text-[10px] text-gray-400 font-medium italic">
                              조회 {pl.viewCount || 0}
                            </span>
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                ) : (
                  /* 데이터가 없을 때 */
                  <div className="flex flex-col items-center justify-center py-24 bg-white/40 backdrop-blur-sm rounded-[3rem] border-2 border-dashed border-gray-300">
                    <div className="text-6xl mb-4">🌙</div>
                    <h4 className="text-xl font-bold text-gray-800">아직 공개된 플리가 없어요</h4>
                    <p className="text-gray-500 mt-2 text-center">첫 번째 플레이리스트의 주인공이 되어보세요!</p>
                    {isLoggedIn && (
                      <button onClick={() => navigate('/mypage')} className="mt-6 px-6 py-2 bg-gray-900 text-white rounded-full font-bold hover:bg-black transition-all">
                        플리 만들러 가기
                      </button>
                    )}
                  </div>
                )}
              </>
            )}
          </div>
        </div>
      </div>
    );
  }
