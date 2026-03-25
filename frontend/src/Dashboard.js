import { useEffect, useState } from "react";
import heroImage from "./assets/plant-showcase-hero.svg";

const plantsData = [
  { 
    id: 1, 
    name: "Monstera Deliciosa", 
    type: "Indoor", 
    price: 25.99, 
    image: "https://growurban.uk/cdn/shop/articles/care-guide-monstera-deliciosa-668092_680bbf00-9564-4f0c-b9cb-27ededaf19d2.jpg?v=1748436514&width=2048" 
  },
  { 
    id: 2, 
    name: "Lavender", 
    type: "Outdoor", 
    price: 12.50, 
    image: "https://www.jungepflanzen.de/media/0d/80/b0/1702551643/lavandula_angustifolia_03.jpg?ts=1702551643" 
  },
  { 
    id: 3, 
    name: "Snake Plant", 
    type: "Indoor", 
    price: 18.00, 
    image: "https://images.squarespace-cdn.com/content/v1/54fbb611e4b0d7c1e151d22a/1610074066643-OP8HDJUWUH8T5MHN879K/Snake+Plant.jpg?format=1000w" 
  },
  { 
    id: 4, 
    name: "Rosemary", 
    type: "Outdoor", 
    price: 10.00, 
    image: "https://bolschare.com/wp-content/uploads/2024/04/rosemary-1090419_1280.webp" 
  }
];

function formatCreatedAt(value) {
  if (!value) {
    return "Not available";
  }
  return new Date(value).toLocaleString("en-US");
}

export default function Dashboard({ user, onLogout }) {
  const [showProfile, setShowProfile] = useState(false);
  const [profile, setProfile] = useState(user);
  
  // --- NUEVOS ESTADOS: Buscador (Andoni) ---
  const [searchTerm, setSearchTerm] = useState("");
  const [filterType, setFilterType] = useState("All");

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
      .catch(() => {});

    return () => {
      ignore = true;
    };
  }, [user]);

  const displayUser = profile ?? user;

  const filteredPlants = plantsData.filter(plant => {
    const matchesSearch = plant.name.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesType = filterType === "All" || plant.type === filterType;
    return matchesSearch && matchesType;
  });

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
          <img src={heroImage} alt="Home with decorative plants" className="hero-image" />
        </div>
      </section>

      <section className="andoni-catalogue">
        <div className="catalogue-header">
          <h2 className="catalogue-title">Plant Catalogue</h2>
          <div className="catalogue-filters">
            <input 
              type="text" 
              className="auth-input search-box" 
              placeholder="Search by name..." 
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
            <select 
              className="auth-input filter-box" 
              value={filterType} 
              onChange={(e) => setFilterType(e.target.value)}
            >
              <option value="All">All Types</option>
              <option value="Indoor">Indoor</option>
              <option value="Outdoor">Outdoor</option>
            </select>
          </div>
        </div>

        <div className="plants-grid">
          {filteredPlants.map(plant => (
            <div key={plant.id} className="auth-card plant-card">
              <div className="plant-image-container">
                <img src={plant.image} alt={plant.name} />
              </div>
              <div className="plant-content">
                <span className="auth-kicker">{plant.type}</span>
                <h3>{plant.name}</h3>
                <p className="price-tag">${plant.price.toFixed(2)}</p>
                <button className="primary-button">View Details</button>
              </div>
            </div>
          ))}
        </div>
        {filteredPlants.length === 0 && (
          <p className="auth-error">No plants match your search criteria.</p>
        )}
      </section>
    </main>
  );
}