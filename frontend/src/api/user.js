import api from "./axios";

export async function getMyProfile() {
  return api.get("/users/me");
}
