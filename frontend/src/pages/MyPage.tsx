import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api/axios";

export default function MyPage() {
  const navigate = useNavigate();
  const [profile, setProfile] = useState(null);
  const [playlists, setPlaylists] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem("accessToken");

    if (!token) {
      navigate("/login");
      return;
    }

    const fetchData = async () => {
      try {
        const [profileRes, playlistRes] = await Promise.all([
          api.get("/users/me"),
          api.get("/users/me/playlists"),
        ]);
        setProfile(profileRes.data);
        setPlaylists(playlistRes.data);
      } catch (e) {
        console.error("마이페이지 조회 실패", e);
        // 토큰이 유효하지 않으면 로그인
        navigate("/login");
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  if (loading) return <div className="p-10">로딩중...</div>;

  return (
    <div>
      <h2>{profile?.name}님의 마이페이지</h2>
      <p>{profile?.email}</p>
      {/* 플레이리스트 렌더링 */}
    </div>
  );
}
