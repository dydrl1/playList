import { Card } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { usePlaylists } from "@/hooks/usePlaylists";

export default function Home() {
  const data = [
    { id: 1, title: "테스트 플레이리스트 1", description: "설명 1" },
    { id: 2, title: "테스트 플레이리스트 2", description: "설명 2" },
  ];
  const isLoading = false;
  const error = null;

  if (isLoading) return <p>로딩 중...</p>;
  if (error) return <p>데이터 로딩 실패</p>;

  return (
    <div className="p-8 grid gap-4">
      {data?.map((playlist) => (
        <Card key={playlist.id} className="p-4">
          <p className="text-xl mb-2">{playlist.title}</p>
          <p className="text-sm mb-2">{playlist.description}</p>
          <Button>재생</Button>
        </Card>
      ))}
    </div>
  );
}
