import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { getMyProfile } from "../api/user";


export default function MyPage() {
  const navigate = useNavigate();
  const [profile, setProfile] = useState(null);

  useEffect(() => {
    console.log("accessToken:", localStorage.getItem("accessToken"));
    getMyProfile()
      .then((res) => setProfile(res.data))
      .catch((err) => {
        // 토큰 문제 → 로그인 페이지로
        if (err.response?.status === 401) {
          localStorage.removeItem("accessToken");
          navigate("/login");
        }
      });
  }, []);

  if (!profile) return <div>로딩중...</div>;

  return <div>안녕하세요 {profile.name}</div>;
}
