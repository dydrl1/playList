import { useState } from "react";
import axios from "axios";
import { useNavigate, Link } from "react-router-dom";
import api from "@/api/axios";

export default function Login() {
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");

  const handleLogin = async () => {
    try {
      const res = await api.post("/auth/login", {
        email,
        password,
      });

      const token = res.data.accessToken;


          localStorage.setItem("accessToken", token);
          window.dispatchEvent(new Event("auth-change"));

          navigate("/"); // 홈으로 이동
        } catch (e) {
          console.error("로그인 실패", e);
        }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-100">
      <div className="w-full max-w-md bg-white/80 backdrop-blur-lg p-8 rounded-2xl shadow-xl">
        <h1 className="text-2xl font-bold text-center mb-6">로그인</h1>

        <div className="flex flex-col gap-4">
          <input
            type="email"
            placeholder="이메일"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            className="px-4 py-3 rounded-lg border focus:outline-none focus:ring-2"
          />

          <input
            type="password"
            placeholder="비밀번호"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            className="px-4 py-3 rounded-lg border focus:outline-none focus:ring-2"
          />

          {/* 회원가입 링크 추가 */}
          <div className="text-center">
            <Link
              to="/signup"
              className="text-sm text-gray-500 hover:text-gray-900 transition-colors"
            >
            아직 회원이 아니신가요? 회원가입하러 가기
            </Link>
          </div>

          {error && (
            <span className="text-sm text-red-500">{error}</span>
          )}

          <button
            onClick={handleLogin}
            className="mt-4 py-3 rounded-lg bg-gray-900 text-white hover:bg-gray-800 transition"
          >
            로그인
          </button>
        </div>
      </div>
    </div>
  );
}
