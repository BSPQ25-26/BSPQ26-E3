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
    <div>
      <h2>Register</h2>
      <div>
        <div>
          <input placeholder="Username" value={username} onChange={e => setUsername(e.target.value)} />
        </div>
        <div>
          <input placeholder="Email" value={email} onChange={e => setEmail(e.target.value)} />
        </div>
        <div>
          <input placeholder="Phone" value={phone} onChange={e => setPhone(e.target.value)} />
        </div>
        <div>
          <input placeholder="Password" type="password" value={password} onChange={e => setPassword(e.target.value)} />
        </div>
      </div>
      <div>
        <button onClick={handleRegister}>Register</button>
        <button onClick={showLogin}>Already have an account? Login</button>
      </div>
      {error && <p style={{ color: "red" }}>{error}</p>}
    </div>
  );
}