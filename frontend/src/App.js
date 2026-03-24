import { useState } from "react";
import Login from "./Login";
import Register from "./Register";
import Dashboard from "./Dashboard";
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

  if (user) {
    return <Dashboard user={user} onLogout={handleLogout} />;
  }

  if (showRegisterForm) {
    return <Register onRegisterSuccess={handleRegisterSuccess} showLogin={showLogin} />;
  }

  return <Login onLoginSuccess={handleLoginSuccess} showRegister={showRegister} successMsg={registerMsg} />;
}

export default App;
