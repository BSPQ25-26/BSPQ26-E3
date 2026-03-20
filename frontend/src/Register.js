import { useState } from "react";

export default function Register({ onRegisterSuccess, showLogin }) {
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [phone, setPhone] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");

  const handleRegister = async () => {
    try {
      const res = await fetch("/api/users", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username, email, phone, password }),
      });

      if (!res.ok) throw new Error("Registration failed");

      const data = await res.json();
      onRegisterSuccess(data);
    } catch (err) {
      console.error(err);
      setError("Registration failed. Try again.");
    }
  };

  return (
    <main className="auth-shell">
      <section className="auth-card">
        <span className="auth-kicker">Green Home</span>
        <h1>Crea tu cuenta</h1>
        <p className="auth-copy">Registra tus datos y entra a la portada principal en cuanto termines.</p>
        <div className="auth-fields">
          <input
            className="auth-input"
            placeholder="Username"
            value={username}
            onChange={(event) => setUsername(event.target.value)}
          />
          <input
            className="auth-input"
            placeholder="Email"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
          />
          <input
            className="auth-input"
            placeholder="Phone"
            value={phone}
            onChange={(event) => setPhone(event.target.value)}
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
          <button className="primary-button" onClick={handleRegister}>Register</button>
          <button className="secondary-button" onClick={showLogin}>Volver al login</button>
        </div>
        {error && <p className="auth-error">{error}</p>}
      </section>
    </main>
  );
}
