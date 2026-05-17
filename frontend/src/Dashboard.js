import { useCallback, useEffect, useRef, useState } from "react";
import heroImage from "./assets/plant-showcase-hero.svg";
import cartEmptyIcon from "./assets/shopping-cart.png";
import cartFilledIcon from "./assets/shopping-cart-filled.png";
import PlantDetailsModal from "./PlantDetailsModal";
import CreatePost from "./CreatePost";
import Cart from "./Cart";
import PurchaseHistory from "./PurchaseHistory";
import SalesHistory from "./SalesHistory";
import { useI18n } from "./i18n/I18nContext";

export default function Dashboard({ user, onLogout }) {
  const { t, formatDate, formatCurrency, translateCategory, translateError } = useI18n();
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
  // Carousel state
  const [activeSlide, setActiveSlide] = useState(0);
  const slideTimerRef = useRef(null);
  // Dashboard tab state
  const [dashboardTab, setDashboardTab] = useState("shop"); // "shop", "purchases", "sales"

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

  useEffect(() => {
    if (!user?.id) return;
    fetch(`/api/carts/${user.id}`)
      .then((res) => (res.ok ? res.json() : null))
      .then((data) => {
        if (data?.items) {
          setCartItemCount(data.items.reduce((sum, item) => sum + item.quantity, 0));
        }
      })
      .catch(() => {});
  }, [user]);

  // Cargar plantas desde la API
  const loadPlants = useCallback(() => {
    let ignore = false;
    setLoading(true);
    setError(null);

    fetch("/api/items")
      .then(async (response) => {
        if (!response.ok) {
          throw new Error(t("errors.failedToFetchItems"));
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
  }, [t]);

  useEffect(() => loadPlants(), [loadPlants]);

  const displayUser = profile ?? user;

  const filteredPlants = plants.filter(plant => {
    const matchesSearch = plant.name.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesType = filterType === "All" || plant.type === filterType;
    return matchesSearch && matchesType;
  });

  const handlePostCreated = () => {
    loadPlants();
  };

  const startSlideTimer = (total) => {
    clearInterval(slideTimerRef.current);
    slideTimerRef.current = setInterval(() => {
      setActiveSlide((prev) => (prev + 1) % total);
    }, 3000);
  };

  useEffect(() => {
    if (plants.length === 0) return;
    startSlideTimer(plants.length);
    return () => clearInterval(slideTimerRef.current);
  }, [plants.length]);

  const goToSlide = (i) => {
    setActiveSlide(i);
    startSlideTimer(plants.length);
  };

  return (
    <main className="dashboard-shell">
      <header className="dashboard-topbar">
        <div>
          <p className="dashboard-eyebrow">{t("common.brand")}</p>
          <h1>{t("common.appName")}</h1>
        </div>
        <div className="topbar-actions">
          <button
            className="create-post-button"
            type="button"
            aria-label={t("dashboard.createPostAria")}
            title={t("dashboard.createPostTitle")}
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
            className="cart-button"
            type="button"
            aria-label={t("dashboard.openCartAria")}
            onClick={() => setShowCart((current) => !current)}
          >
            <img 
              src={cartItemCount > 0 ? cartFilledIcon : cartEmptyIcon} 
              alt={t("dashboard.cartAlt")}
              className="cart-button-icon"
            />
            {cartItemCount > 0 && (
              <span className="cart-badge">{cartItemCount}</span>
            )}
          </button>

          {showCart && (
            <Cart
              userId={user.id}
              onClose={() => setShowCart(false)}
              onCartLoad={setCartItemCount}
            />
          )}

          <div className="profile-area">
            <button
              className="profile-button"
              type="button"
              aria-label={t("dashboard.profileAria")}
              onClick={() => setShowProfile((current) => !current)}
            >
              <svg viewBox="0 0 24 24" aria-hidden="true">
                <path d="M12 12a4.25 4.25 0 1 0-4.25-4.25A4.26 4.26 0 0 0 12 12Zm0 2.25c-3.9 0-7 2.01-7 4.5V20h14v-1.25c0-2.49-3.1-4.5-7-4.5Z" />
              </svg>
            </button>
            {showProfile && (
              <aside className="profile-card">
                <p className="profile-card-title">{t("dashboard.profileTitle")}</p>
                <dl className="profile-details">
                  <div>
                    <dt>{t("common.labels.username")}</dt>
                    <dd>{displayUser.username || t("common.notAvailable")}</dd>
                  </div>
                  <div>
                    <dt>{t("common.labels.email")}</dt>
                    <dd>{displayUser.email || t("common.notAvailable")}</dd>
                  </div>
                  <div>
                    <dt>{t("common.labels.phone")}</dt>
                    <dd>{displayUser.phone || t("common.notAvailable")}</dd>
                  </div>
                  <div>
                    <dt>{t("common.labels.created")}</dt>
                    <dd>{formatDate(displayUser.createdAt, {
                      year: "numeric",
                      month: "short",
                      day: "numeric",
                      hour: "2-digit",
                      minute: "2-digit",
                    })}</dd>
                  </div>
                </dl>
                <button className="secondary-button profile-logout" type="button" onClick={onLogout}>
                  {t("common.actions.signOut")}
                </button>
              </aside>
            )}
          </div>
        </div>
      </header>

      <section className="hero-panel">
        <div className="hero-copy">
          <span className="auth-kicker">{t("common.home")}</span>
          <h2>{t("dashboard.welcome", { name: user.username ?? user.email ?? t("common.userFallback") })}</h2>
        </div>
        <div className="hero-image-frame hero-carousel-frame">
          {plants.length > 0 ? (
            <>
              <div
                className="hero-carousel-track"
                style={{ transform: `translateX(-${activeSlide * 100}%)` }}
              >
                {plants.map((plant) => (
                  <div key={plant.id} className="hero-carousel-slide">
                    <img src={plant.image} alt={plant.name} />
                    <div className="hero-carousel-label">{plant.name}</div>
                  </div>
                ))}
              </div>
              <div className="hero-carousel-dots">
                {plants.map((_, i) => (
                  <button
                    key={i}
                    className={`hero-carousel-dot${i === activeSlide ? " active" : ""}`}
                    onClick={() => goToSlide(i)}
                    aria-label={t("dashboard.carouselAria", { index: i + 1 })}
                  />
                ))}
              </div>
            </>
          ) : (
            <img src={heroImage} alt={t("dashboard.heroImageAlt")} className="hero-image" />
          )}
        </div>
      </section>

      <section className="andoni-catalogue">
        <div className="catalogue-header">
          <h2 className="catalogue-title">{t("dashboard.catalogueTitle")}</h2>
          <div className="dashboard-tabs">
            <button 
              className={`tab-button${dashboardTab === "shop" ? " active" : ""}`}
              onClick={() => setDashboardTab("shop")}
            >
              {t("dashboard.shoppingTab")}
            </button>
            <button 
              className={`tab-button${dashboardTab === "purchases" ? " active" : ""}`}
              onClick={() => setDashboardTab("purchases")}
            >
              {t("dashboard.purchasesTab")}
            </button>
            <button 
              className={`tab-button${dashboardTab === "sales" ? " active" : ""}`}
              onClick={() => setDashboardTab("sales")}
            >
              {t("dashboard.salesTab")}
            </button>
          </div>
        </div>

        {dashboardTab === "shop" && (
          <>
            <div className="catalogue-filters">
              <input 
                type="text" 
                className="auth-input search-box" 
                placeholder={t("dashboard.searchPlaceholder")}
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
              />
              <select 
                className="auth-input filter-box" 
                value={filterType} 
                onChange={(e) => setFilterType(e.target.value)}
              >
                <option value="All">{t("common.categories.allTypes")}</option>
                <option value="Indoor">{t("common.categories.indoor")}</option>
                <option value="Outdoor">{t("common.categories.outdoor")}</option>
                <option value="Succulent">{t("common.categories.succulent")}</option>
                <option value="Flowering">{t("common.categories.flowering")}</option>
              </select>
            </div>

            <div className="plants-grid" data-testid="plants-grid">
              {loading ? (
                <p className="auth-error">{t("dashboard.loadingPlants")}</p>
              ) : error ? (
                <p className="auth-error">Error: {translateError(error)}</p>
              ) : filteredPlants.length > 0 ? (
                filteredPlants.map(plant => (
                  <div key={plant.id} className="auth-card plant-card">
                    <div className="plant-image-container">
                      <img src={plant.image} alt={plant.name} />
                    </div>
                    <div className="plant-content">
                      <span className="auth-kicker">{translateCategory(plant.type)}</span>
                      <h3>{plant.name}</h3>
                      <p className="price-tag">{formatCurrency(plant.price)}</p>
                      <button 
                        className="primary-button"
                        onClick={() => setSelectedPlantId(plant.id)}
                      >
                        {t("common.actions.viewDetails")}
                      </button>
                    </div>
                  </div>
                ))
              ) : (
                <p className="auth-error">{t("dashboard.noPlantsMatch")}</p>
              )}
            </div>
          </>
        )}

        {dashboardTab === "purchases" && (
          <PurchaseHistory userId={user.id} />
        )}

        {dashboardTab === "sales" && (
          <SalesHistory userId={user.id} />
        )}
      </section>
      {selectedPlantId && (
        <PlantDetailsModal
          plantId={selectedPlantId}
          userId={user.id}
          onClose={() => setSelectedPlantId(null)}
          onItemAdded={(qty) => setCartItemCount((c) => c + qty)}
        />
      )}
    </main>
  );
}
