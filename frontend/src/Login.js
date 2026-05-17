import { useState } from "react";
import { useI18n } from "./i18n/I18nContext";

export default function Login({ onLoginSuccess, showRegister, successMsg }) {
  const { t, translateError } = useI18n();
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
        setError(translateError(bodyText, "errors.invalidCredentials"));
        return;
      }

      const data = await res.json();
      onLoginSuccess({ ...data.profile, email: data.email });
    } catch (err) {
      console.error("Login fetch error:", err);
      setError(t("errors.connection"));
    }
  };

  const handleResetPassword = async () => {
    setResetStatus("");
    if (!email) {
      setResetStatus(`error:${t("errors.resetEmailMissing")}`);
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
        setResetStatus(`error:${translateError(text, "errors.resetFailed")}`);
      }
    } catch {
      setResetStatus(`error:${t("errors.connection")}`);
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
        setResendStatus(`error:${translateError(text, "errors.resendFailed")}`);
      }
    } catch {
      setResendStatus(`error:${t("errors.connection")}`);
    }
  };

  return (
    <main className="auth-shell">
      <section className="auth-card">
        <span className="auth-kicker">{t("common.brand")}</span>
        <h1>{t("auth.signInTitle")}</h1>
        <p className="auth-copy">{t("auth.signInCopy")}</p>

        {successMsg && (
          <p className="auth-notice">{successMsg}</p>
        )}

        <div className="auth-fields">
          <input
            className="auth-input"
            data-testid="login-email"
            placeholder={t("common.labels.email")}
            type="email"
            value={email}
            onChange={e => { setEmail(e.target.value); setResetStatus(""); }}
          />
          <input
            className="auth-input"
            data-testid="login-password"
            placeholder={t("common.labels.password")}
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
              {t("auth.forgotPassword")}
            </button>
          </div>
          {resetStatus === "ok" && (
            <p className="auth-notice" style={{ margin: 0 }}>{t("auth.resetEmailSent")}</p>
          )}
          {resetStatus.startsWith("error:") && (
            <p className="auth-error" style={{ margin: 0 }}>{resetStatus.slice(6)}</p>
          )}
        </div>
        <div className="auth-actions">
          <button className="primary-button" data-testid="login-submit" onClick={handleLogin}>{t("common.actions.login")}</button>
          <button className="secondary-button" data-testid="login-go-register" onClick={showRegister}>{t("common.actions.createAccount")}</button>
        </div>

        {emailNotConfirmed && (
          <div className="auth-notice">
            {t("auth.confirmEmailPrompt")}
            {resendStatus === "ok" ? (
              <><br />{t("auth.confirmationEmailSent")}</>
            ) : (
              <>
                <br />
                <button className="auth-notice-action" onClick={handleResend}>
                  {t("auth.resendConfirmation")}
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

        {error && <p className="auth-error" data-testid="login-error">{error}</p>}
      </section>
    </main>
  );
}
