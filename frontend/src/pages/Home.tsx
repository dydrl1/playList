import { useState, useEffect, useRef } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";
import { Link } from "react-router-dom";

interface Track {
  title: string;
  artist: string;
  tumbnail: string
  }

export default function Home() {
  const [query, setQuery] = useState("");
  const [results, setResults] = useState<Track[]>([]);
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

        {/* Pinterest Style Grid */}
        <div className="grid grid-cols-5 gap-4 w-full max-w-[1200px] mt-8 mx-auto">
          {results.map((item, idx) => (
            <div
              key={idx}
              className="flex flex-col bg-white/90 rounded-lg shadow-md overflow-hidden cursor-pointer animate-fade-in hover:shadow-xl transition-shadow"
            >
              <img
                src={item.imageUrl}           // thumbnail → imageUrl
                alt={item.title}
                className="w-full h-48 object-cover"
              />
              <div className="p-2 flex flex-col gap-1">
                <span className="font-semibold text-gray-800 text-sm truncate">{item.title}</span>
                <span className="text-gray-500 text-xs truncate">{item.artist}</span>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
