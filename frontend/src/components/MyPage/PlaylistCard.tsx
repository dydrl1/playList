interface Playlist {
  id: number;
  title: string;
  trackCount: number;
  createdAt: string;
}

interface Props {
  playlists: Playlist[];
}

export default function PlaylistGrid({ playlists }: Props) {
  return (
    <div>
      <h3 className="text-lg font-semibold mb-4">내 플레이리스트</h3>

      {playlists.length === 0 ? (
        <p className="text-gray-500">아직 만든 플레이리스트가 없습니다.</p>
      ) : (
        <div className="grid grid-cols-4 gap-4">
          {playlists.map((pl) => (
            <div
              key={pl.id}
              className="bg-white rounded-lg shadow p-4 hover:shadow-lg transition"
            >
              <h4 className="font-semibold truncate">{pl.title}</h4>
              <p className="text-sm text-gray-500">
                {pl.trackCount}곡
              </p>
              <p className="text-xs text-gray-400">
                {pl.createdAt}
              </p>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
