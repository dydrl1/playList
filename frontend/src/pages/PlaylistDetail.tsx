import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { usePlayer } from "@/components/PlayerContext";
import api from "@/api/axios";
import { DragDropContext, Droppable, Draggable, DropResult } from "@hello-pangea/dnd";


// 1. 인터페이스 확장
interface TrackDto {
  id: number;
  playlistTrackId: number;
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
      // 1. 상세 데이터 호출
      const res = await api.get(`/api/playlists/${playlistId}`);
      const playlistData = res.data.data || res.data;

      setData(playlistData);

      // 2. 폼 업데이트 (수정 권한이 있을 때만 의미가 있겠지만 일단 유지)
      setEditForm({
        title: playlistData.title,
        description: playlistData.description || "",
        public: playlistData.public ?? true
      });
    } catch (err: any) {
      console.error("데이터 로딩 에러:", err);

      const status = err.response?.status;

      if (status === 401) {
        // 케이스 A: 로그인이 안 되어 있음.
        // 하지만 백엔드 permitAll이 정상 작동한다면 여기로 오지 않아야 합니다.
        // 만약 여기로 왔다면 백엔드 SecurityConfig 설정을 다시 점검해야 해요!
        alert("로그인이 필요하거나 접근 권한이 없습니다.");
        navigate("/login");
      } else if (status === 403) {
        // 케이스 B: 로그인은 했으나 남의 비공개 플리를 보려고 함
        alert("비공개 플레이리스트입니다. 🔒");
        navigate("/");
      } else {
        // 케이스 C: 기타 서버 에러 등
        alert("정보를 불러오는데 실패했습니다.");
        navigate(-1);
      }
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

const { playTrack } = usePlayer();

const handlePlayButtonClick = async (trackId: number) => {
  // trackId가 없거나 undefined인 경우 실행 중단
    if (!trackId) {
      console.error("trackId가 존재하지 않습니다!");
      return;
    }
  try {
    const res = await api.get(`/tracks/${trackId}/play`);
    playTrack(res.data.data); // 전역 상태 업데이트 -> 하단바 재생 시작
  } catch (err) {
    alert("재생 정보를 가져올 수 없습니다.");
  }
};


// 트랙 삭제 함수
const handleRemoveTrack = async (playlistTrackId: number) => {
  if (!window.confirm("플레이리스트에서 이 곡을 제거할까요? 🎵")) return;

  try {
    await api.delete(`/api/playlists/tracks/${playlistTrackId}`);

    // UI 상태 업데이트: setData를 사용하여 tracks 배열 필터링
    setData((prev) => {
      if (!prev) return prev;
      return {
        ...prev,
        tracks: prev.tracks.filter((t: any) => t.playlistTrackId !== playlistTrackId),
      };
    });

    alert("곡이 제거되었습니다.");
  } catch (err: any) {
    alert("삭제 실패: " + (err.response?.data?.message || "알 수 없는 오류"));
  }
};

  if (loading) return <div className="p-10 text-center font-bold">로딩중...</div>;
  if (!data) return null;

  // 드래그 종료 핸들러 수정
    const onDragEnd = async (result: DropResult) => {
      if (!result.destination || !data) return;

      // 1. UI 선반영 (Optimistic UI)
      const items = Array.from(data.tracks);
      const [reorderedItem] = items.splice(result.source.index, 1);
      items.splice(result.destination.index, 0, reorderedItem);
      setData({ ...data, tracks: items }); // 화면을 먼저 업데이트

      // 2. 백엔드 API 전송
      try {
        await api.put(`/api/playlists/${playlistId}/track/order`, {
          trackIds: items.map(t => t.playlistTrackId)
        });
        // ✅ 중요: 성공 시 fetchDetail()을 호출하지 않습니다!
        // 서버에는 이미 저장이 되었고, 화면(State)도 이미 최신화되었기 때문입니다.
        // 이렇게 하면 GET 요청이 발생하지 않아 Redis 조회수 로직도 타지 않습니다.
      } catch (e) {
        console.error("순서 저장 실패", e);
        alert("순서 저장에 실패했습니다.");
        fetchDetail(); // ❌ 실패했을 때만 원복을 위해 서버 데이터를 다시 가져옴
      }
    };

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

    const handleUpdate = async () => {
      try {
        const requestData = {
          title: editForm.title,
          description: editForm.description,
          isPublic: editForm.public
        };
        await api.patch(`/api/me/playlists/${playlistId}`, requestData);
        alert("수정되었습니다. ✨");
        setIsEditing(false);
        fetchDetail();
      } catch (err) {
        alert("수정에 실패했습니다.");
      }
    };



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
                  {editForm.public ? "전체 공개 🌍" : "나만 보기 🔒"}
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
        <h2 className="text-2xl font-bold text-gray-800 mb-6 px-2">
                  수록곡 ({data.tracks.length})
                  {data.owner && <span className="text-sm font-normal text-gray-500 ml-3">드래그하여 순서를 바꿀 수 있습니다.</span>}
                </h2>

                {/* --- 드래그 앤 드롭 영역 --- */}
        <DragDropContext onDragEnd={onDragEnd}>
                  <Droppable droppableId="tracks-grid" direction="horizontal">
                    {(provided) => (
                      <div {...provided.droppableProps} ref={provided.innerRef} className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-5 gap-6 pb-20">
                        {data.tracks.map((track, index) => (
                          <Draggable key={track.playlistTrackId} draggableId={String(track.playlistTrackId)} index={index} isDragDisabled={!data.owner}>
                            {(provided, snapshot) => (
                              <div
                                ref={provided.innerRef}
                                {...provided.draggableProps}
                                {...provided.dragHandleProps}
                                className={`bg-white p-4 rounded-[2rem] shadow-sm transition-all group relative ${
                                  snapshot.isDragging ? "shadow-2xl ring-2 ring-black z-50 scale-105" : "hover:shadow-2xl hover:-translate-y-1"
                                }`}
                              >
                                <div className="relative aspect-square rounded-2xl overflow-hidden mb-3 group"> {/* group 클래스 확인! */}
                                  <img src={track.imageUrl} alt={track.title} className="w-full h-full object-cover" />

                                  {/* 공통 오버레이: 마우스 호버 시 어두워짐 */}
                                  <div className="absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity duration-300" />

                                  {/* 1. 중앙 재생 버튼: 누구나 보임 */}
                                  <div className="absolute inset-0 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center">
                                    <button
                                      onClick={(e) => {
                                        e.stopPropagation();
                                        console.log("클릭한 트랙 정보:", track);
                                        handlePlayButtonClick(track.trackId); // track.id는 서버에서 조회용 PK
                                      }}
                                      className="bg-white p-4 rounded-full shadow-xl hover:scale-110 active:scale-90 transition-all text-black"
                                    >
                                      <svg className="w-6 h-6 fill-current" viewBox="0 0 24 24">
                                        <path d="M8 5v14l11-7z" />
                                      </svg>
                                    </button>
                                  </div>

                                  {/* 2. 우측 상단 삭제 버튼: 주인(owner)에게만 보임 */}
                                  {data.owner && (
                                    <div className="absolute top-2 right-2 opacity-0 group-hover:opacity-100 transition-opacity">
                                      <button
                                        onClick={(e) => {
                                          e.stopPropagation();
                                          handleRemoveTrack(track.playlistTrackId); // playlistTrackId는 매핑 테이블용 PK
                                        }}
                                        className="bg-white/90 p-2 rounded-full hover:bg-red-500 hover:text-white transition-all shadow-lg active:scale-90"
                                      >
                                        <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={2.5} stroke="currentColor" className="w-4 h-4">
                                          <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
                                        </svg>
                                      </button>
                                    </div>
                                  )}
                                </div>
                                <p className="font-bold text-gray-800 truncate">{track.title}</p>
                                <p className="text-xs text-gray-500 truncate">{track.artist}</p>
                              </div>
                            )}
                          </Draggable>
                        ))}
                        {provided.placeholder}
                      </div>
                    )}
                  </Droppable>
                </DragDropContext>
      </div>
    </div>
  );
}