import { useState } from "react";
import Login from "./Login";
import Register from "./Register";
import Dashboard from "./Dashboard";

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

  if (user) {
    return <Dashboard user={user} />;
  }

  if (showRegisterForm) {
    return <Register onRegisterSuccess={handleRegisterSuccess} />;
  }

  return <Login onLoginSuccess={handleLoginSuccess} showRegister={showRegister} />;
}

export default App;