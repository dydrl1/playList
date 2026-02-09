import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import api from "@/api/axios";

// 1. 인터페이스 확장
interface TrackDto {
  id: number;
  title: string;
  artist: string;
  imageUrl: string;
}

interface PlaylistDetail {
  playlistId: number;
  title: string;
  description: string;
  ownerName: string;
  viewCount?: number;
  public: boolean;
  owner: boolean;
  tracks: TrackDto[];
}

export default function PlaylistDetail() {
  const { playlistId } = useParams();
  const navigate = useNavigate();

  const [data, setData] = useState<PlaylistDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [isEditing, setIsEditing] = useState(false);

  // 1. 초기값은 안전하게 빈 객체로 시작
  const [editForm, setEditForm] = useState({
    title: "",
    description: "",
    public: true
  });

  // 2. 함수를 하나로 통일 (컴포넌트 내부에 정의)
  const fetchDetail = async () => {
    try {
      setLoading(true);
      const res = await api.get(`/api/playlists/${playlistId}`);
      const playlistData = res.data.data || res.data;

      setData(playlistData);

      // 데이터를 가져온 후 폼 업데이트
      setEditForm({
        title: playlistData.title,
        description: playlistData.description || "",
        public: playlistData.public ?? true // null 방어 코드
      });
    } catch (err: any) {
      console.error("데이터 로딩 에러:", err);
      if (err.response?.status === 403) {
        alert("비공개 플레이리스트입니다.");
      } else {
        alert("정보를 불러오는데 실패했습니다.");
      }
      navigate(-1);
    } finally {
      setLoading(false);
    }
  };

  // 3. useEffect에서는 선언된 함수를 실행만 시킴
  useEffect(() => {
    if (playlistId) {
      fetchDetail();
    }
  }, [playlistId]);


  // 삭제 핸들러
  const handleDelete = async () => {
    if (!window.confirm("정말로 이 플레이리스트를 삭제하시겠습니까?")) return;
    try {
      await api.delete(`/api/me/playlists/${playlistId}`);
      alert("삭제되었습니다.");
      navigate("/mypage");
    } catch (err) {
      alert("삭제에 실패했습니다.");
    }
  };

  // 수정 핸들러
  const handleUpdate = async () => {
    try {
      // 1. 백엔드 DTO(isPublic)와 프론트 상태(public)의 이름을 맞춰줍니다.
      const requestData = {
        title: editForm.title,
        description: editForm.description,
        isPublic: editForm.public // 👈 여기서 이름을 바꿔서 전송!
      };

      console.log("백엔드로 보내는 데이터:", requestData);

      // 2. PATCH 요청 보낼 때 requestData 사용
      await api.patch(`/api/me/playlists/${playlistId}`, requestData);

      alert("수정되었습니다. ✨");
      setIsEditing(false);
      fetchDetail(); // 수정된 'public' 값을 서버에서 다시 읽어옴
    } catch (err) {
      console.error("수정 실패:", err);
      alert("수정에 실패했습니다.");
    }
  };

  if (loading) return <div className="p-10 text-center font-bold">로딩중...</div>;
  if (!data) return null;

  return (
    <div className="min-h-screen bg-gray-50 relative overflow-hidden">
      <div className="absolute inset-0 bg-gradient-to-b from-gray-200 to-white h-80" />

      <div className="relative z-10 max-w-6xl mx-auto pt-16 px-6">
        <div className="bg-white/40 backdrop-blur-md rounded-[3rem] p-8 mb-12 border border-white/50 shadow-xl">
          {isEditing ? (
            /* --- 수정 모드 UI --- */
            <div className="flex flex-col gap-4">
              <input
                className="text-4xl font-black bg-white/80 border-b-2 border-black outline-none p-2 rounded-t-lg"
                value={editForm.title}
                onChange={(e) => setEditForm({...editForm, title: e.target.value})}
                placeholder="제목을 입력하세요"
              />
              <textarea
                className="text-lg bg-white/80 border rounded-xl p-4 outline-none min-h-[100px]"
                value={editForm.description}
                onChange={(e) => setEditForm({...editForm, description: e.target.value})}
                placeholder="플레이리스트에 대한 설명을 적어주세요"
              />
              <div className="flex items-center gap-4 mt-2">
                <span className="font-bold text-gray-700">공개 설정</span>
                <button
                  onClick={() => setEditForm({...editForm, public: !editForm.public})}
                  className={`px-6 py-2 rounded-full text-sm font-bold transition-all ${
                    editForm.public ? 'bg-black text-white' : 'bg-gray-300 text-gray-600'
                  }`}
                >
                  {editForm.public ? "지구본 공개 🌍" : "나만 보기 🔒"}
                </button>
              </div>
              <div className="flex gap-2 mt-6">
                <button onClick={handleUpdate} className="bg-blue-600 text-white px-8 py-3 rounded-2xl font-bold hover:bg-blue-700 transition-all">변경사항 저장</button>
                <button onClick={() => setIsEditing(false)} className="bg-gray-200 text-gray-700 px-8 py-3 rounded-2xl font-bold hover:bg-gray-300 transition-all">취소</button>
              </div>
            </div>
          ) : (
            /* --- 일반 보기 UI --- */
            <div className="flex flex-col md:flex-row gap-8 items-start md:items-end">
              <div className="w-64 h-64 bg-gray-200 rounded-[2.5rem] shadow-2xl overflow-hidden flex-shrink-0">
                {data.tracks[0]?.imageUrl ? (
                  <img src={data.tracks[0].imageUrl} className="w-full h-full object-cover" alt="Cover" />
                ) : (
                  <div className="w-full h-full flex items-center justify-center text-6xl">🎧</div>
                )}
              </div>
              <div className="flex-1">
                <div className="flex items-center gap-4 mb-2">
                  <h1 className="text-5xl font-black text-gray-900">{data.title}</h1>
                  {data.owner && (
                    <div className="flex gap-2">
                      <button onClick={() => setIsEditing(true)} className="p-3 bg-white/80 rounded-full hover:bg-black hover:text-white transition-all shadow-sm">✏️</button>
                      <button onClick={handleDelete} className="p-3 bg-white/80 rounded-full hover:bg-red-500 hover:text-white transition-all shadow-sm">🗑️</button>
                    </div>
                  )}
                </div>
                <p className="text-gray-600 text-lg mb-4">{data.description || "설명이 없는 플레이리스트입니다."}</p>
                <div className="flex items-center gap-4 text-sm font-bold text-gray-500">
                  <span>작성자: {data.ownerName}</span>
                  <span>•</span>
                  <span>조회수 {(data.viewCount || 0).toLocaleString()}</span>
                  <span>•</span>
                  <span className={`px-3 py-1 rounded-full text-[10px] ${data.public ? 'bg-green-100 text-green-700' : 'bg-gray-200 text-gray-600'}`}>
                    {data.public ? "PUBLIC" : "PRIVATE"}
                  </span>
                </div>
              </div>
            </div>
          )}
        </div>

        {/* 수록곡 섹션 (기존 코드 유지) */}
        <h2 className="text-2xl font-bold text-gray-800 mb-6 px-2">수록곡 ({data.tracks.length})</h2>
        <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-5 gap-6 pb-20">
          {data.tracks.map((track) => (
            <div key={track.id} className="bg-white p-4 rounded-[2rem] shadow-sm hover:shadow-2xl hover:-translate-y-1 transition-all group">
              {/* ... 트랙 아이템 디자인 ... */}
              <div className="relative aspect-square rounded-2xl overflow-hidden mb-3">
                <img src={track.imageUrl} alt={track.title} className="w-full h-full object-cover" />
              </div>
              <p className="font-bold text-gray-800 truncate">{track.title}</p>
              <p className="text-xs text-gray-500 truncate">{track.artist}</p>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}