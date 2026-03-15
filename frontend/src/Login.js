import { useState } from "react";

export default function Login({ onLoginSuccess, showRegister }) {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");

  const handleLogin = async () => {
    try {
      const res = await fetch("/api/users/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username, password }),
      });

      console.log("Login response status:", res.status);

      if (res.status === 404) {
        showRegister(); // user not found, shows register page
        return;
      }

      if (!res.ok) {
        let bodyText = "";
        try {
          bodyText = await res.text();
        } catch (e) {
        }
        console.error("Login failed:", res.status, bodyText);
        setError(`Login failed (${res.status}). ${bodyText || "Try again."}`);
        return;
      }

      const data = await res.json();
      onLoginSuccess(data); // login successful, access dashboard
    } catch (err) {
      console.error("Login fetch error:", err);
      setError("Login failed. Try again.");
    }
  };

  return (
    <div>
      <h2>Login</h2>
      <div>
        <div>
          <input placeholder="Username" value={username} onChange={e => setUsername(e.target.value)} />
        </div>
        <div>
          <input placeholder="Password" type="password" value={password} onChange={e => setPassword(e.target.value)} />
        </div>
      </div>
      <div>
        <button onClick={handleLogin}>Login</button>
        <button onClick={showRegister}>Don't have an account? Register</button>
      </div>
      {error && <p style={{ color: "red" }}>{error}</p>}
    </div>
  );
}