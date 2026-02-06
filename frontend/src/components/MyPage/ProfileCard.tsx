interface Props {
  profile: {
    name: string;
    email: string;
    createdAt: string;
  };
}

export default function ProfileCard({ profile }: Props) {
  return (
    <div className="bg-white rounded-xl shadow p-6 flex justify-between items-center">
      <div>
        <h2 className="text-xl font-bold">{profile.name}</h2>
        <p className="text-gray-500">{profile.email}</p>
      </div>
      <span className="text-sm text-gray-400">
        가입일: {profile.createdAt}
      </span>
    </div>
  );
}
