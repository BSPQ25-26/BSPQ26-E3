import { useEffect, useState } from "react";
import heroImage from "./assets/plant-showcase-hero.svg";
import cartEmptyIcon from "./assets/icons/shopping-cart.png";
import cartFilledIcon from "./assets/icons/shopping-cart-filled.png";
import PlantDetailsModal from "./PlantDetailsModal";
import CreatePost from "./CreatePost";
import Cart from "./Cart";

function formatCreatedAt(value) {
  if (!value) {
    return "Not available";
  }
  return new Date(value).toLocaleString("en-US");
}

export default function Dashboard({ user, onLogout }) {
  const [showProfile, setShowProfile] = useState(false);
  const [profile, setProfile] = useState(user);
  
  // Estados para el buscador y las plantas
  const [searchTerm, setSearchTerm] = useState("");
  const [filterType, setFilterType] = useState("All");
  const [plants, setPlants] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  
  // Estado para el modal de detalles
  const [selectedPlantId, setSelectedPlantId] = useState(null);
  // Estado del carrito
  const [showCart, setShowCart] = useState(false);
  const [cartItemCount, setCartItemCount] = useState(0);
  // Estado para el modal de crear post
  const [showCreatePost, setShowCreatePost] = useState(false);

  // Cargar datos del perfil
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

  // Cargar plantas desde la API
  const loadPlants = () => {
    let ignore = false;
    setLoading(true);
    setError(null);

    fetch("/api/items")
      .then(async (response) => {
        if (!response.ok) {
          throw new Error("Failed to fetch items");
        }
        return response.json();
      })
      .then((data) => {
        if (!ignore) {
          const transformedPlants = data.map(item => ({
            id: item.id,
            name: item.title,
            type: item.categoryName,
            price: item.amount,
            image: item.image_url || "https://via.placeholder.com/300"
          }));
          setPlants(transformedPlants);
        }
      })
      .catch((err) => {
        if (!ignore) {
          setError(err.message);
          console.error("Error loading plants:", err);
        }
      })
      .finally(() => {
        if (!ignore) {
          setLoading(false);
        }
      });

    return () => {
      ignore = true;
    };
  };

  useEffect(() => {
    return loadPlants();
  }, []);

  const displayUser = profile ?? user;

  const filteredPlants = plants.filter(plant => {
    const matchesSearch = plant.name.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesType = filterType === "All" || plant.type === filterType;
    return matchesSearch && matchesType;
  });

  const handlePostCreated = () => {
    loadPlants();
  };

  return (
    <main className="dashboard-shell">
      <header className="dashboard-topbar">
        <div>
          <p className="dashboard-eyebrow">Green Home</p>
          <h1>Planthub</h1>
        </div>
        <div className="topbar-actions">
          <button
            className="create-post-button"
            type="button"
            aria-label="Create new post"
            title="Create a new plant post"
            onClick={() => setShowCreatePost(true)}
          >
            +
          </button>

          {showCreatePost && (
            <CreatePost 
              userId={user.id}
              onClose={() => setShowCreatePost(false)}
              onPostCreated={handlePostCreated}
            />
          )}

          <button
            className={`cart-button ${cartItemCount > 0 ? 'cart-button--active' : ''}`}
            type="button"
            aria-label="Open shopping cart"
            onClick={() => setShowCart((current) => !current)}
          >
            <img 
              src={cartItemCount > 0 ? cartFilledIcon : cartEmptyIcon} 
              alt="Shopping cart"
              className="cart-button-icon"
            />
            {cartItemCount > 0 && (
              <span className="cart-badge">{cartItemCount}</span>
            )}
          </button>

          {showCart && (
            <Cart userId={user.id} onClose={() => setShowCart(false)} onCartUpdate={setCartItemCount} />
          )}

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
              <option value="Succulent">Succulent</option>
              <option value="Flowering">Flowering</option>
            </select>
          </div>
        </div>

        <div className="plants-grid">
          {loading ? (
            <p className="auth-error">Cargando plantas...</p>
          ) : error ? (
            <p className="auth-error">Error: {error}</p>
          ) : filteredPlants.length > 0 ? (
            filteredPlants.map(plant => (
              <div key={plant.id} className="auth-card plant-card">
                <div className="plant-image-container">
                  <img src={plant.image} alt={plant.name} />
                </div>
                <div className="plant-content">
                  <span className="auth-kicker">{plant.type}</span>
                  <h3>{plant.name}</h3>
                  <p className="price-tag">${plant.price.toFixed(2)}</p>
                  <button 
                    className="primary-button"
                    onClick={() => setSelectedPlantId(plant.id)}
                  >
                    View Details
                  </button>
                </div>
              </div>
            ))
          ) : (
            <p className="auth-error">No plants match your search criteria.</p>
          )}
        </div>
      </section>
      {selectedPlantId && (
        <PlantDetailsModal 
          plantId={selectedPlantId} 
          userId={user.id}
          onClose={() => setSelectedPlantId(null)}
        />
      )}
    </main>
  );
}