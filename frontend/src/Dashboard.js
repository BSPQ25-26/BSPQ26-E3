import { useEffect, useState } from "react";
import heroImage from "./assets/plant-showcase-hero.svg";

function formatCreatedAt(value) {
  if (!value) {
    return "No disponible";
  }

  return new Date(value).toLocaleString("es-ES");
}

export default function Dashboard({ user, onLogout }) {
  const [showProfile, setShowProfile] = useState(false);
  const [profile, setProfile] = useState(user);

  useEffect(() => {
    setProfile(user);

    const query = new URLSearchParams();
    if (user?.email) {
      query.set("email", user.email);
    }
    if (user?.username) {
      query.set("username", user.username);
    }

    if (!query.toString()) {
      return undefined;
    }

    let ignore = false;

    fetch(`/api/users/profile?${query.toString()}`)
      .then(async (response) => {
        if (!response.ok) {
          return null;
        }

        return response.json();
      })
      .then((data) => {
        if (!ignore && data) {
          setProfile((current) => ({
            ...current,
            ...data,
          }));
        }
      })
      .catch(() => {
      });

    return () => {
      ignore = true;
    };
  }, [user]);

  const displayUser = profile ?? user;

  return (
    <main className="dashboard-shell">
      <header className="dashboard-topbar">
        <div>
          <p className="dashboard-eyebrow">Green Home</p>
          <h1>Plathub</h1>
        </div>
        <div className="profile-area">
          <button
            className="profile-button"
            type="button"
            aria-label="Mostrar informacion de usuario"
            onClick={() => setShowProfile((current) => !current)}
          >
            <svg viewBox="0 0 24 24" aria-hidden="true">
              <path d="M12 12a4.25 4.25 0 1 0-4.25-4.25A4.26 4.26 0 0 0 12 12Zm0 2.25c-3.9 0-7 2.01-7 4.5V20h14v-1.25c0-2.49-3.1-4.5-7-4.5Z" />
            </svg>
          </button>
          {showProfile && (
            <aside className="profile-card">
              <p className="profile-card-title">Tu perfil</p>
              <dl className="profile-details">
                <div>
                  <dt>Usuario</dt>
                  <dd>{displayUser.username || "No disponible"}</dd>
                </div>
                <div>
                  <dt>Email</dt>
                  <dd>{displayUser.email || "No disponible"}</dd>
                </div>
                <div>
                  <dt>Telefono</dt>
                  <dd>{displayUser.phone || "No disponible"}</dd>
                </div>
                <div>
                  <dt>Creado</dt>
                  <dd>{formatCreatedAt(displayUser.createdAt)}</dd>
                </div>
              </dl>
              <button className="secondary-button profile-logout" type="button" onClick={onLogout}>
                Cerrar sesion
              </button>
            </aside>
          )}
        </div>
      </header>

      <section className="hero-panel">
        <div className="hero-copy">
          <span className="auth-kicker">Portada</span>
          <h2>Bienvenido, {user.username ?? user.email ?? "usuario"}</h2>
        </div>
        <div className="hero-image-frame">
          <img src={heroImage} alt="Portada con plantas decorativas en un espacio luminoso" className="hero-image" />
        </div>
      </section>
    </main>
  );
}
