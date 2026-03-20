import { useState } from "react";
import Login from "./Login";
import Register from "./Register";
import Dashboard from "./Dashboard";
import "./App.css";

function App() {
  const [user, setUser] = useState(null);
  const [showRegisterForm, setShowRegisterForm] = useState(false);

  const handleLoginSuccess = (userData) => {
    setUser(userData);
  };

  const handleRegisterSuccess = (userData) => {
    setUser(userData);
  };

  const showRegister = () => {
    setShowRegisterForm(true);
  };

  const showLogin = () => {
    setShowRegisterForm(false);
  };

  const handleLogout = () => {
    setUser(null);
    setShowRegisterForm(false);
  };

  if (user) {
    return <Dashboard user={user} onLogout={handleLogout} />;
  }

  if (showRegisterForm) {
    return <Register onRegisterSuccess={handleRegisterSuccess} showLogin={showLogin} />;
  }

  return <Login onLoginSuccess={handleLoginSuccess} showRegister={showRegister} />;
}

export default App;
