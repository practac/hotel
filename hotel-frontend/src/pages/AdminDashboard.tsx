import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import "../App.css";

const API_BASE = import.meta.env.VITE_API_BASE ?? "http://localhost:8080";

type Room = {
  id: number;
  roomNumber: string;
  slug: string;
  status: string;
};

function AdminDashboard() {
  const [rooms, setRooms] = useState<Room[]>([]);
  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const token = localStorage.getItem("adminToken") ?? "";

  const fetchRooms = async () => {
    setLoading(true);
    try {
      const res = await fetch(`${API_BASE}/api/admin/rooms`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      if (res.status === 401 || res.status === 403) {
        navigate("/admin/login");
        return;
      }
      if (!res.ok) {
        setMessage(`불러오기 실패: ${await res.text()}`);
        return;
      }
      const data = (await res.json()) as Room[];
      setRooms(data);
      setMessage("");
    } catch (e) {
      setMessage("불러오는 중 오류가 발생했습니다.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (!token) {
      navigate("/admin/login");
      return;
    }
    fetchRooms();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleReset = async (slug: string) => {
    setMessage("");
    try {
      const res = await fetch(`${API_BASE}/api/admin/reset/${slug}`, {
        method: "POST",
        headers: { Authorization: `Bearer ${token}` },
      });
      const text = await res.text();
      if (!res.ok) {
        setMessage(`Reset 실패: ${text}`);
        return;
      }
      setMessage(text);
      fetchRooms();
    } catch (e) {
      setMessage("Reset 중 오류가 발생했습니다.");
    }
  };

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <h2 className="title">Admin Dashboard</h2>
          <div className="muted">방 상태를 확인하고 OCCUPIED로 리셋할 수 있습니다.</div>
        </div>
        <button
          className="btn btn-ghost"
          onClick={() => {
            localStorage.removeItem("adminToken");
            navigate("/admin/login");
          }}
        >
          Logout
        </button>
      </div>

      {message && <div className="alert">{message}</div>}

      <div className="card">
        {loading ? (
          <p>불러오는 중...</p>
        ) : (
          <table className="table">
            <thead>
              <tr>
                <th>Room</th>
                <th>Slug</th>
                <th>Status</th>
                <th style={{ width: 150 }}></th>
              </tr>
            </thead>
            <tbody>
              {rooms.map((room) => (
                <tr key={room.id}>
                  <td>{room.roomNumber}</td>
                  <td style={{ fontFamily: "monospace", fontSize: 13 }}>{room.slug}</td>
                  <td>
                    <span
                      className={`badge ${
                        room.status === "CHECKED_OUT"
                          ? "warn"
                          : room.status === "OCCUPIED"
                          ? "success"
                          : "neutral"
                      }`}
                    >
                      {room.status}
                    </span>
                  </td>
                  <td>
                    <button className="btn btn-primary" onClick={() => handleReset(room.slug)}>
                      Reset to OCCUPIED
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}

export default AdminDashboard;
