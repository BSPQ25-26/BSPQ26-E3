import { useState } from "react";

export default function Register({ onRegisterSuccess, showLogin }) {
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [phone, setPhone] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");

  const handleRegister = async () => {
    setError("");
    try {
      const res = await fetch("/api/users", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username, email, phone, password }),
      });

      if (!res.ok) {
        const bodyText = await res.text().catch(() => "");
        setError(bodyText || "Registration failed. Please try again.");
        return;
      }

      onRegisterSuccess("Account created. Check your email to confirm your address before signing in.");
    } catch (err) {
      console.error(err);
      setError("Could not connect. Please try again.");
    }
  };

  return (
    <main className="auth-shell">
      <section className="auth-card">
        <span className="auth-kicker">Green Home</span>
        <h1>Create account</h1>
        <p className="auth-copy">Register your details and access the main page once you're done.</p>
        <div className="auth-fields">
          <input
            className="auth-input"
            placeholder="Username"
            value={username}
            onChange={e => setUsername(e.target.value)}
          />
          <input
            className="auth-input"
            placeholder="Email"
            type="email"
            value={email}
            onChange={e => setEmail(e.target.value)}
          />
          <input
            className="auth-input"
            placeholder="Phone"
            value={phone}
            onChange={e => setPhone(e.target.value)}
          />
          <input
            className="auth-input"
            placeholder="Password"
            type="password"
            value={password}
            onChange={e => setPassword(e.target.value)}
          />
        </div>
        <div className="auth-actions">
          <button className="primary-button" onClick={handleRegister}>Register</button>
          <button className="secondary-button" onClick={showLogin}>Back to login</button>
        </div>
        {error && <p className="auth-error">{error}</p>}
      </section>
    </main>
  );
}
