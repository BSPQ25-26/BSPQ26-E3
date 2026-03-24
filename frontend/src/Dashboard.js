import { useEffect, useState } from "react";
import heroImage from "./assets/plant-showcase-hero.svg";

function formatCreatedAt(value) {
  if (!value) {
    return "Not available";
  }

  return new Date(value).toLocaleString("en-US");
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
            aria-label="Show user information"
            onClick={() => setShowProfile((current) => !current)}
          >
            <svg viewBox="0 0 24 24" aria-hidden="true">
              <path d="M12 12a4.25 4.25 0 1 0-4.25-4.25A4.26 4.26 0 0 0 12 12Zm0 2.25c-3.9 0-7 2.01-7 4.5V20h14v-1.25c0-2.49-3.1-4.5-7-4.5Z" />
            </svg>
          </button>
          {showProfile && (
            <aside className="profile-card">
              <p className="profile-card-title">Your profile</p>
              <dl className="profile-details">
                <div>
                  <dt>Username</dt>
                  <dd>{displayUser.username || "Not available"}</dd>
                </div>
                <div>
                  <dt>Email</dt>
                  <dd>{displayUser.email || "Not available"}</dd>
                </div>
                <div>
                  <dt>Phone</dt>
                  <dd>{displayUser.phone || "Not available"}</dd>
                </div>
                <div>
                  <dt>Created</dt>
                  <dd>{formatCreatedAt(displayUser.createdAt)}</dd>
                </div>
              </dl>
              <button className="secondary-button profile-logout" type="button" onClick={onLogout}>
                Sign out
              </button>
            </aside>
          )}
        </div>
      </header>

      <section className="hero-panel">
        <div className="hero-copy">
          <span className="auth-kicker">Home</span>
          <h2>Welcome, {user.username ?? user.email ?? "user"}</h2>
        </div>
        <div className="hero-image-frame">
          <img src={heroImage} alt="Home with decorative plants in a bright space" className="hero-image" />
        </div>
      </section>
    </main>
  );
}
