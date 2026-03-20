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
    <main className="auth-shell">
      <section className="auth-card">
        <span className="auth-kicker">Green Home</span>
        <h1>Inicia sesion</h1>
        <p className="auth-copy">Accede a tu espacio y entra directamente a la portada principal del proyecto.</p>
        <div className="auth-fields">
          <input
            className="auth-input"
            placeholder="Username"
            value={username}
            onChange={(event) => setUsername(event.target.value)}
          />
          <input
            className="auth-input"
            placeholder="Password"
            type="password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
          />
        </div>
        <div className="auth-actions">
          <button className="primary-button" onClick={handleLogin}>Login</button>
          <button className="secondary-button" onClick={showRegister}>Crear cuenta</button>
        </div>
        {error && <p className="auth-error">{error}</p>}
      </section>
    </main>
  );
}
