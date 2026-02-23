import axios from "axios";

const api = axios.create({
  baseURL: "http://localhost:8080",
  withCredentials: true, // 쿠키 사용 시 필요
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem("accessToken");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401) {
      const refreshToken = localStorage.getItem("refreshToken");
      // 수정: 리프레시 토큰이 없으면 강제 이동 대신 에러만 던짐
      if (!refreshToken) {
         return Promise.reject(error);
      }

      try {
        const res = await axios.post(
          "http://localhost:8080/auth/refresh",
          { refreshToken }
        );
        const newAccessToken = res.headers["authorization"]?.substring(7);
        if (newAccessToken) localStorage.setItem("accessToken", newAccessToken);

        error.config.headers["Authorization"] = `Bearer ${newAccessToken}`;
        return axios(error.config);
      } catch {
        localStorage.removeItem("accessToken");
        localStorage.removeItem("refreshToken");
        return Promise.reject(error);
      }
    }
    return Promise.reject(error);
  }
);

export default api;
