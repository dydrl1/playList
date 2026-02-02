import { useQuery } from "@tanstack/react-query";
import { api } from "@/api/client";
import { PlaylistCard } from "@/types/playlist";

export const usePlaylists = () => {
  return useQuery<PlaylistCard[]>({
    queryKey: ["playlists"],
    queryFn: async () => {
      const res = await api.get("/playlists"); // GET /playlists API
      return res.data;
    },
  });
};
