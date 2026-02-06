import api from "./axios";

/**
 * 내 정보 조회
 * GET /users/me
 */
export const getMyProfile = async () => {
  const res = await api.get("/users/me");
  return res.data;
};

/**
 * 내 플레이리스트 조회
 * GET /users/me/playlists
 */
export const getMyPlaylists = async () => {
  const res = await api.get("/users/me/playlists");
  return res.data;
};
