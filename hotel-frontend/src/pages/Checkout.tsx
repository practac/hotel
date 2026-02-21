import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";

const API_BASE = import.meta.env.VITE_API_BASE ?? "http://localhost:8080"; // fallback to local backend

function Checkout() {
  const { slug } = useParams();
  const [roomNumber, setRoomNumber] = useState<string>("");
  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!slug) return;
    (async () => {
      try {
        const res = await fetch(`${API_BASE}/api/rooms/${slug}/number`);
        if (res.ok) {
          setRoomNumber(await res.text());
        }
      } catch (e) {
        // ignore; fallback to slug display
      }
    })();
  }, [slug]);

  const handleCheckout = async () => {
    if (!slug) {
      setMessage("방 식별자를 알 수 없습니다.");
      return;
    }
    setLoading(true);
    setMessage("토큰 발급 중...");

    try {
      const tokenRes = await fetch(
        `${API_BASE}/api/checkout/${slug}/token`
      );
      if (!tokenRes.ok) {
        const err = await tokenRes.text();
        setMessage(`토큰 발급 실패: ${err}`);
        setLoading(false);
        return;
      }
      const token = await tokenRes.text();

      setMessage("체크아웃 처리 중...");
      const res = await fetch(
        `${API_BASE}/api/checkout/${slug}?token=${encodeURIComponent(
          token
        )}`,
        { method: "POST" }
      );

      const data = await res.text();
      if (!res.ok) {
        setMessage(`실패: ${data}`);
      } else {
        setMessage(data);
      }
    } catch (e) {
      setMessage("요청 중 오류가 발생했습니다.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ textAlign: "center", marginTop: "100px" }}>
      <h2>Room {roomNumber || slug || "(unknown)"}</h2>
      <button onClick={handleCheckout} disabled={loading}>
        {loading ? "처리 중..." : "Checkout"}
      </button>
      <p>{message}</p>
    </div>
  );
}

export default Checkout;
