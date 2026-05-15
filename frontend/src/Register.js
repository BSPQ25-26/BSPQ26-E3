import { useState } from "react";
import { useI18n } from "./i18n/I18nContext";

export default function Register({ onRegisterSuccess, showLogin }) {
  const { t, translateError } = useI18n();
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
        setError(translateError(bodyText, "errors.registrationFailed"));
        return;
      }

      onRegisterSuccess(t("auth.accountCreated"));
    } catch (err) {
      console.error(err);
      setError(t("errors.connection"));
    }
  };

  return (
    <main className="auth-shell">
      <section className="auth-card">
        <span className="auth-kicker">{t("common.brand")}</span>
        <h1>{t("auth.createAccountTitle")}</h1>
        <p className="auth-copy">{t("auth.createAccountCopy")}</p>
        <div className="auth-fields">
          <input
            className="auth-input"
            placeholder={t("common.labels.username")}
            value={username}
            onChange={e => setUsername(e.target.value)}
          />
          <input
            className="auth-input"
            placeholder={t("common.labels.email")}
            type="email"
            value={email}
            onChange={e => setEmail(e.target.value)}
          />
          <input
            className="auth-input"
            placeholder={t("common.labels.phone")}
            value={phone}
            onChange={e => setPhone(e.target.value)}
          />
          <input
            className="auth-input"
            placeholder={t("common.labels.password")}
            type="password"
            value={password}
            onChange={e => setPassword(e.target.value)}
          />
        </div>
        <div className="auth-actions">
          <button className="primary-button" onClick={handleRegister}>{t("common.actions.register")}</button>
          <button className="secondary-button" onClick={showLogin}>{t("common.actions.backToLogin")}</button>
        </div>
        {error && <p className="auth-error">{error}</p>}
      </section>
    </main>
  );
}
