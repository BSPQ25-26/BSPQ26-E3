import { useState } from "react";

export default function Login({ onLoginSuccess, showRegister, successMsg }) {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [emailNotConfirmed, setEmailNotConfirmed] = useState(false);
  const [resendStatus, setResendStatus] = useState("");
  const [resetStatus, setResetStatus] = useState("");

  const handleLogin = async () => {
    setError("");
    setEmailNotConfirmed(false);
    setResendStatus("");
    try {
      const res = await fetch("/api/users/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, password }),
      });

      if (res.status === 404) {
        showRegister();
        return;
      }

      if (res.status === 403) {
        setEmailNotConfirmed(true);
        return;
      }

      if (!res.ok) {
        const bodyText = await res.text().catch(() => "");
        setError(bodyText || "Invalid credentials. Please try again.");
        return;
      }

      const data = await res.json();
      onLoginSuccess({ ...data.profile, email: data.email });
    } catch (err) {
      console.error("Login fetch error:", err);
      setError("Could not connect. Please try again.");
    }
  };

  const handleResetPassword = async () => {
    setResetStatus("");
    if (!email) {
      setResetStatus("error:Enter your email above first.");
      return;
    }
    try {
      const res = await fetch("/api/users/reset-password", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email }),
      });
      if (res.ok) {
        setResetStatus("ok");
      } else {
        const text = await res.text().catch(() => "");
        setResetStatus("error:" + (text || "Could not send reset email."));
      }
    } catch {
      setResetStatus("error:Could not connect. Please try again.");
    }
  };

  const handleResend = async () => {
    setResendStatus("");
    try {
      const res = await fetch("/api/users/resend-confirmation", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email }),
      });
      const text = await res.text().catch(() => "");
      if (res.ok) {
        setResendStatus("ok");
      } else {
        setResendStatus("error:" + (text || "Could not resend."));
      }
    } catch (err) {
      setResendStatus("error:Could not connect.");
    }
  };

  return (
    <main className="auth-shell">
      <section className="auth-card">
        <span className="auth-kicker">Green Home</span>
        <h1>Sign in</h1>
        <p className="auth-copy">Access your space and go directly to the main page of the project.</p>

        {successMsg && (
          <p className="auth-notice">{successMsg}</p>
        )}

        <div className="auth-fields">
          <input
            className="auth-input"
            placeholder="Email"
            type="email"
            value={email}
            onChange={e => { setEmail(e.target.value); setResetStatus(""); }}
          />
          <input
            className="auth-input"
            placeholder="Password"
            type="password"
            value={password}
            onChange={e => setPassword(e.target.value)}
          />
          <div style={{ display: "flex", justifyContent: "flex-end" }}>
            <button
              type="button"
              onClick={handleResetPassword}
              style={{ background: "none", border: "none", padding: 0, fontSize: "0.82rem", color: "#4a7c59", cursor: "pointer", textDecoration: "underline" }}
            >
              Forgot password?
            </button>
          </div>
          {resetStatus === "ok" && (
            <p className="auth-notice" style={{ margin: 0 }}>Reset email sent. Check your inbox.</p>
          )}
          {resetStatus.startsWith("error:") && (
            <p className="auth-error" style={{ margin: 0 }}>{resetStatus.slice(6)}</p>
          )}
        </div>
        <div className="auth-actions">
          <button className="primary-button" onClick={handleLogin}>Login</button>
          <button className="secondary-button" onClick={showRegister}>Create account</button>
        </div>

        {emailNotConfirmed && (
          <div className="auth-notice">
            Please confirm your email before signing in. Check your inbox.
            {resendStatus === "ok" ? (
              <><br />Confirmation email sent. Check your inbox.</>
            ) : (
              <>
                <br />
                <button className="auth-notice-action" onClick={handleResend}>
                  Resend confirmation email
                </button>
                {resendStatus.startsWith("error:") && (
                  <span style={{ display: "block", marginTop: 6, color: "#9b2d23", fontSize: "0.88rem" }}>
                    {resendStatus.slice(6)}
                  </span>
                )}
              </>
            )}
          </div>
        )}

        {error && <p className="auth-error">{error}</p>}
      </section>
    </main>
  );
}
