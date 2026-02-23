import { BrowserRouter as Router, Routes, Route, Navigate } from "react-router-dom";
import Home from "@/pages/Home";
import Login from "@/pages/Login";
import Signup from "@/pages/signup";
import MyPage from "@/pages/MyPage";
import PlaylistDetail from "@/pages/PlaylistDetail";
import PlayerBar from "@/components/PlayerBar"; // 재생바 컴포넌트
import { PlayerProvider } from "@/components/PlayerContext"; // 전역 상태 context

// 보호된 라우트 컴포넌트
const ProtectedRoute = ({ children }: { children: React.ReactNode }) => {
  const token = localStorage.getItem("accessToken");
  if (!token) {
    alert("로그인이 필요한 페이지입니다. 🔒");
    return <Navigate to="/login" replace />;
  }
  return <>{children}</>;
};

export default function App() {
  return (
    // 1. PlayerProvider로 감싸서 모든 페이지에서 재생 상태 공유
    <PlayerProvider>
      <Router>
        <div className="min-h-screen bg-background text-foreground pb-24">
          {/* pb-24: 하단 재생바에 콘텐츠가 가려지지 않도록 여백 추가 */}

          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/login" element={<Login />} />
            <Route path="/signup" element={<Signup />} />

            {/* 2. 마이페이지 보호 적용 */}
            <Route
              path="/mypage"
              element={
                <ProtectedRoute>
                  <MyPage />
                </ProtectedRoute>
              }
            />

            <Route path="/playlists/:playlistId" element={<PlaylistDetail />} />
          </Routes>

          {/* 3. PlayerBar를 Routes 밖에 두어 페이지 이동 시에도 리렌더링 방지 */}
          <PlayerBar />
        </div>
      </Router>
    </PlayerProvider>
  );
}