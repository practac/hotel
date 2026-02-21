import { useState } from "react";
import { useNavigate } from "react-router-dom";
import "../App.css";

const API_BASE = import.meta.env.VITE_API_BASE ?? "http://localhost:8080";

function AdminLogin() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setMessage("");
    try {
      const res = await fetch(`${API_BASE}/api/admin/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username, password }),
      });
      const text = await res.text();
      if (!res.ok) {
        setMessage(`로그인 실패: ${text}`);
        return;
      }
      localStorage.setItem("adminToken", text);
      navigate("/admin/dashboard");
    } catch (err) {
      setMessage("로그인 중 오류가 발생했습니다.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page" style={{ maxWidth: 500 }}>
      <div className="card" style={{ padding: "32px" }}>
        <h2 className="title" style={{ marginBottom: 12 }}>Admin Login</h2>
        <p className="muted" style={{ marginBottom: 22 }}>대시보드 접근을 위해 로그인하세요.</p>
        <form onSubmit={handleLogin} style={{ display: "flex", flexDirection: "column", gap: 14 }}>
          <input
            className="input"
            placeholder="Username"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            required
          />
          <input
            className="input"
            placeholder="Password"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
          <button className="btn btn-primary" type="submit" disabled={loading}>
            {loading ? "로그인 중..." : "Login"}
          </button>
        </form>
        {message && <div className="alert" style={{ marginTop: 16 }}>{message}</div>}
      </div>
    </div>
  );
}

export default AdminLogin;
