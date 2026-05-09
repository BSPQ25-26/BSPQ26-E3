import { useState } from "react";
import Login from "./Login";
import Register from "./Register";
import Dashboard from "./Dashboard";
import LanguageSwitcher from "./LanguageSwitcher";
import { I18nProvider } from "./i18n/I18nContext";
import "./App.css";

function App() {
  const [user, setUser] = useState(null);
  const [showRegisterForm, setShowRegisterForm] = useState(false);
  const [registerMsg, setRegisterMsg] = useState("");

  const handleLoginSuccess = (userData) => {
    setUser(userData);
  };

  const handleRegisterSuccess = (msg) => {
    setRegisterMsg(msg);
    setShowRegisterForm(false);
  };

  const showRegister = () => {
    setRegisterMsg("");
    setShowRegisterForm(true);
  };

  const showLogin = () => {
    setShowRegisterForm(false);
  };

  const handleLogout = () => {
    setUser(null);
    setShowRegisterForm(false);
    setRegisterMsg("");
  };

  let content;

  if (user) {
    content = <Dashboard user={user} onLogout={handleLogout} />;
  } else if (showRegisterForm) {
    content = <Register onRegisterSuccess={handleRegisterSuccess} showLogin={showLogin} />;
  } else {
    content = <Login onLoginSuccess={handleLoginSuccess} showRegister={showRegister} successMsg={registerMsg} />;
  }

  return (
    <I18nProvider>
      <LanguageSwitcher />
      {content}
    </I18nProvider>
  );
}

export default App;
