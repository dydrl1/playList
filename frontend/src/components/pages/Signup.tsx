import { useState } from "react";
import axios from "axios";
import { useNavigate, Link } from "react-router-dom";
import api from "@/api/axios";

export default function Signup() {
  const navigate = useNavigate();
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");

  const handleSignup = async () => {
    try {
      // backend API 호출
      const res = await api.post("/auth/signup", {
        name,
        email,
        password,
      });

      // 가입 성공 후 로그인 페이지로 이동
      navigate("/login");
    } catch (e: any) {
      // 에러 처리
      setError(
        e.response?.data?.message || "회원가입 중 오류가 발생했습니다."
      );
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-100">
      <div className="w-full max-w-md bg-white/80 backdrop-blur-lg p-8 rounded-2xl shadow-xl">
        <h1 className="text-2xl font-bold text-center mb-6">회원가입</h1>

        <div className="flex flex-col gap-4">
          <input
            type="text"
            placeholder="이름"
            value={name}
            onChange={(e) => setName(e.target.value)}
            className="px-4 py-3 rounded-lg border focus:outline-none focus:ring-2"
          />

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

          {error && <span className="text-sm text-red-500">{error}</span>}

          <button
            onClick={handleSignup}
            className="mt-4 py-3 rounded-lg bg-gray-900 text-white hover:bg-gray-800 transition"
          >
            회원가입
          </button>

          {/* 로그인 페이지로 이동 링크 */}
          <div className="text-right mt-2">
            <Link
              to="/login"
              className="text-sm text-gray-500 hover:text-gray-900 transition-colors"
            >
              이미 계정이 있으신가요? 로그인
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
}
