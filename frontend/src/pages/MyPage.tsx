import { useEffect, useState, useRef } from "react";
import { useNavigate, Link } from "react-router-dom";
import api from "../api/axios";

interface Track {
  title: string;
  artist: string;
  imageUrl: string;
}

interface UserProfile {
  name: string;
  email: string;
}

export default function MyPage() {
  const navigate = useNavigate();
  const containerRef = useRef<HTMLDivElement>(null);

  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [playlists, setPlaylists] = useState<any[]>([]); // 내 플레이리스트 목록
  const [tracks, setTracks] = useState<Track[]>([]);     // 내 노래 목록
  const [loading, setLoading] = useState(true);

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [newPlaylistName, setNewPlaylistName] = useState("");
  const [isEditOrderMode, setIsEditOrderMode] = useState(false);

  // 프로필 수정 관련 상태
    const [isEditingProfile, setIsEditingProfile] = useState(false);
    const [editName, setEditName] = useState("");

  const fetchData = async () => {
    try {
      const [profileRes, playlistRes] = await Promise.all([
        api.get("/users/me"),
        api.get("/api/me/playlists"),
      ]);
      const userData = profileRes.data.data || profileRes.data;
      setProfile(profileRes.data.data || profileRes.data);
      setEditName(userData.name);
      setPlaylists(playlistRes.data.data || playlistRes.data);
    } catch (e) {
      console.error("조회 실패", e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handleCreatePlaylist = async () => {
    if (!newPlaylistName.trim()) return;
    try {
        await api.post("/api/me/playlists", {
          title: newPlaylistName // 백엔드 PlaylistCreateRequest의 필드명에 맞춤
        });

        setNewPlaylistName("");
        setIsModalOpen(false);
        fetchData(); // 성공 후 목록 새로고침
      } catch (e) {
        console.error("생성 실패:", e);
        alert("플레이리스트 생성에 실패했습니다.");
      }
  };

  const handleLogout = () => {
    localStorage.removeItem("accessToken");
    window.dispatchEvent(new Event("auth-change"));
    navigate("/");
  };

  // 1. 프로필 수정 (PATCH /users/me)
  const handleUpdateProfile = async () => {
    if (!editName.trim()) return;
    try {
      await api.patch("/users/me", { name: editName });
      setIsEditingProfile(false);
      fetchData(); // 정보 갱신
      alert("프로필이 수정되었습니다.");
    } catch (e) {
      console.error("수정 실패", e);
      alert("프로필 수정에 실패했습니다.");
    }
  };

  // 2. 회원 탈퇴 (DELETE /users/me)
  const handleDeleteAccount = async () => {
    if (window.confirm("정말로 탈퇴하시겠습니까? 모든 데이터가 삭제되며 복구할 수 없습니다.")) {
      try {
        await api.delete("/users/me");
        alert("회원 탈퇴가 완료되었습니다.");
        handleLogout(); // 로그아웃 처리 및 메인 이동
      } catch (e) {
        console.error("탈퇴 실패", e);
        alert("회원 탈퇴 처리에 실패했습니다.");
      }
    }
  };


  if (loading) return <div className="min-h-screen flex items-center justify-center bg-gray-200 font-bold">로딩중...</div>;

  return (
      <div className="relative min-h-screen w-full overflow-hidden">
        {/* 배경 */}
        <div className="absolute inset-0 bg-gradient-to-br from-gray-200 via-gray-300 to-gray-400 blur-3xl scale-110" />

        {/* Content */}
        <div ref={containerRef} className="relative z-10 flex flex-col items-center pt-10 px-10 h-screen overflow-auto">

          {/* 상단 네비게이션 */}
          <div className="w-full max-w-[1200px] flex justify-between items-center mb-10">
            <Link to="/">
              <img src="/src/assets/logo.png" alt="Logo" className="w-32 hover:opacity-80 transition-opacity" />
            </Link>
            <button
                        onClick={handleLogout}
                        className="px-5 py-2 rounded-full text-gray-700 font-medium hover:bg-black hover:text-white hover:border-black transition-all"
                      >
                        로그아웃
                      </button>
          </div>

          {/* 프로필 섹션 & 생성 버튼 */}
          <div className="w-full max-w-[1200px] flex flex-col md:flex-row justify-between items-start md:items-center mb-12 gap-6 bg-white/30 p-8 rounded-[2.5rem] backdrop-blur-md border border-white/20">
                    <div className="flex items-center gap-6">
                      <div className="w-24 h-24 bg-black rounded-full flex items-center justify-center text-white text-3xl font-bold shadow-xl">
                        {profile?.name?.charAt(0)}
                      </div>

                      {isEditingProfile ? (
                        <div className="flex flex-col gap-2">
                          <input
                            value={editName}
                            onChange={(e) => setEditName(e.target.value)}
                            className="text-2xl font-extrabold bg-white/50 border-b-2 border-black outline-none px-2"
                            autoFocus
                          />
                          <div className="flex gap-2">
                            <button onClick={handleUpdateProfile} className="text-sm font-bold text-blue-600">저장</button>
                            <button onClick={() => setIsEditingProfile(false)} className="text-sm font-bold text-gray-500">취소</button>
                          </div>
                        </div>
                      ) : (
                        <div>
                          <div className="flex items-center gap-3">
                            <h2 className="text-3xl font-extrabold text-gray-900">{profile?.name}님</h2>
                            <button
                              onClick={() => setIsEditingProfile(true)}
                              className="p-1 hover:bg-black/5 rounded-full transition-colors"
                            >
                              <svg className="w-5 h-5 text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z" />
                              </svg>
                            </button>
                          </div>
                          <p className="text-gray-600">{profile?.email}</p>
                        </div>
                      )}
                    </div>
                    <button
                      onClick={() => setIsModalOpen(true)}
                      className="px-6 py-3 bg-black text-white rounded-2xl font-bold shadow-lg hover:bg-gray-800 transition-all transform hover:scale-105"
                    >
                      + 새 플레이리스트
                    </button>
                  </div>


          {/* Pinterest Style Grid - 플레이리스트 목록 전용 */}
          <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-5 gap-4 w-full max-w-[1200px] mt-8 mx-auto mb-20">
            {playlists.map((pl) => (
              <div
                key={pl.id}
                onClick={() => navigate(`/playlists/${pl.id}`)}
                className="group relative flex flex-col bg-white/90 rounded-3xl shadow-md overflow-hidden cursor-pointer animate-fade-in hover:shadow-xl transition-all duration-300 border border-white/20"
              >
                {/* 1. 이미지 섹션 */}
                <div className="relative h-48 overflow-hidden bg-gradient-to-br from-gray-100 to-gray-300 flex items-center justify-center">
                  {pl.thumbnailUrl ? (
                    <img
                      src={pl.thumbnailUrl}
                      alt={pl.title}
                      className="w-full h-full object-cover group-hover:scale-110 transition-transform duration-500"
                    />
                  ) : (
                    <svg className="w-16 h-16 text-gray-400 group-hover:text-black transition-colors" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="1.5" d="M9 19V6l12-3v13M9 19c0 1.105-1.343 2-3 2s-3-.895-3-2 1.343-2 3-2 3 .895 3 2zm12-3c0 1.105-1.343 2-3 2s-3-.895-3-2 1.343-2 3-2 3 .895 3 2z" />
                    </svg>
                  )}

                  {/* Hover Overlay */}
                  <div className="absolute inset-0 bg-black/20 opacity-0 group-hover:opacity-100 transition-opacity duration-300 flex items-center justify-center">
                    <span className="text-white font-bold bg-black/40 px-4 py-2 rounded-full backdrop-blur-sm">상세보기</span>
                  </div>
                </div>

                {/* 2. 텍스트 정보 영역 */}
                <div className="p-4 flex flex-col gap-1 bg-white/80 backdrop-blur-sm">
                  <div className="flex justify-between items-start">
                    <span className="font-bold text-gray-900 text-base truncate flex-1">
                      {pl.title}
                    </span>
                    {/* 내 플리지만 다른 사람들에게 받은 좋아요 수를 표시 */}
                    {pl.likeCount > 0 && (
                      <span className="text-[10px] text-red-500 font-bold ml-2">
                        ❤️ {pl.likeCount}
                      </span>
                    )}
                  </div>

                  <div className="flex justify-between items-center mt-2">
                    <div className="flex gap-2 items-center">
                      <span className="text-gray-500 text-xs font-medium">
                        곡 {pl.trackCount || 0}개
                      </span>
                      <span className="text-[10px] text-gray-400">
                        조회 {pl.viewCount || 0}
                      </span>
                    </div>

                    <span className={`text-[10px] px-2 py-0.5 rounded-full font-bold ${
                      pl.isPublic
                        ? "bg-green-100 text-green-600"
                        : "bg-gray-100 text-gray-400"
                    }`}>
                      {pl.isPublic ? "공개" : "비공개"}
                    </span>
                  </div>
                </div>
              </div>
            ))}

            {/* 플레이리스트가 하나도 없을 때 보여줄 안내 카드 */}
            {playlists.length === 0 && (
               <div
                 onClick={() => setIsModalOpen(true)}
                 className="flex flex-col items-center justify-center h-48 bg-white/40 border-2 border-dashed border-gray-400 rounded-lg cursor-pointer hover:bg-white/60 transition-all col-span-full"
               >
                 <p className="text-gray-500 font-medium">생성된 플레이리스트가 없습니다.</p>
                 <p className="text-gray-400 text-sm">새 리스트를 만들어 노래를 담아보세요!</p>
               </div>
            )}
          </div>

          {/* [변경] 페이지 최하단: 회원탈퇴 영역 (위험 구역) */}
                  <div className="w-full max-w-[1200px] mt-auto mb-10 pt-10 border-t border-gray-300/50 flex justify-between items-center text-gray-500">
                    <p className="text-sm">© 2024 MyMusic Archive. All rights reserved.</p>
                    <button
                      onClick={handleDeleteAccount}
                      className="text-sm hover:text-red-500 hover:underline transition-colors"
                    >
                      회원 탈퇴하기
                    </button>
                  </div>

        {/* 모달 부분 (기존과 동일) */}
        {isModalOpen && (
          <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/30 backdrop-blur-sm animate-fade-in">
            <div className="w-full max-w-sm bg-white/90 backdrop-blur-2xl p-8 rounded-[2.5rem] shadow-2xl border border-white/50">
              <h3 className="text-2xl font-bold text-gray-900 mb-6">새 리스트 만들기</h3>
              <input
                autoFocus
                type="text"
                placeholder="이름을 입력하세요"
                value={newPlaylistName}
                onChange={(e) => setNewPlaylistName(e.target.value)}
                onKeyDown={(e) => e.key === "Enter" && handleCreatePlaylist()}
                className="w-full px-6 py-4 rounded-2xl bg-gray-100/50 focus:outline-none focus:ring-2 focus:ring-black mb-6"
              />
              <div className="flex gap-3">
                <button onClick={() => setIsModalOpen(false)} className="flex-1 py-4 text-gray-500 font-bold hover:bg-gray-100 rounded-2xl transition-all">취소</button>
                <button onClick={handleCreatePlaylist} className="flex-1 py-4 bg-gray-900 text-white font-bold rounded-2xl hover:bg-black transition-all">생성</button>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}