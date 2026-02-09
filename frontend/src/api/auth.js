import api from "./axios";

export async function login(email, password) {
  const res = await api.post("/auth/login", { email, password });

  const authHeader = res.headers["authorization"];
  const refreshTokenHeader = res.headers["refresh-token"];

//  console.log("authHeader:", authHeader);
//  console.log("refreshTokenHeader:", refreshTokenHeader);

  if (!authHeader || !authHeader.startsWith("Bearer ")) {
    throw new Error("로그인 토큰이 없습니다.");
  }

  localStorage.setItem("accessToken", authHeader.substring(7));

  if (refreshTokenHeader) {
    localStorage.setItem("refreshToken", refreshTokenHeader);
  }

  return res.data; // userInfo
}

export function logout() {
  localStorage.removeItem("accessToken");
  localStorage.removeItem("refreshToken");
}
