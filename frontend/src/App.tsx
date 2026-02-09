import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import Home from "@/pages/Home";
import Login from "@/pages/Login";
import Signup from "@/pages/signup";
import MyPage from "@/pages/MyPage";
import PlaylistDetail from "@/pages/PlaylistDetail";

export default function App() {
  return (
    <Router>
      <div className="min-h-screen bg-background text-foreground">
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/login" element={<Login />} />
          <Route path="/signup" element={<Signup />} />
          <Route path="/mypage" element={<MyPage />} />
          <Route path="/playlists/:playlistId" element={<PlaylistDetail />} />
        </Routes>
      </div>
    </Router>
  );
}
