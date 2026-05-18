import React from "react";
import { useI18n } from "./i18n/I18nContext";
import CommentSection from './CommentSection';

export default function PlantDetailsModal({ plantId, userId, onClose, onItemAdded }) {
  const { t, formatCurrency, translateCategory, translateItemStatus, translateError } = useI18n();
  const [plantDetails, setPlantDetails] = React.useState(null);
  const [loading, setLoading] = React.useState(false);
  const [quantity, setQuantity] = React.useState(1);
  const [addingToCart, setAddingToCart] = React.useState(false);
  const [message, setMessage] = React.useState(null);

  React.useEffect(() => {
    setLoading(true);

    fetch(`/api/items/${plantId}`)
      .then(async (response) => {
        if (!response.ok) {
          throw new Error(t("errors.failedToFetchPlantDetails"));
        }
        return response.json();
      })
      .then((data) => {
        setPlantDetails(data);
        setLoading(false);
      })
      .catch((err) => {
        console.error("Error loading plant details:", err);
        setLoading(false);
      });
  }, [plantId, t]);

  const handleAddToCart = async () => {
    if (!userId) {
      setMessage({ type: "error", text: t("plantDetails.userIdNotFound") });
      return;
    }

    setAddingToCart(true);
    setMessage(null);

    try {
      const response = await fetch(`/api/carts/${userId}/items`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          itemId: plantId,
          quantity,
        }),
      });

      if (!response.ok) {
        let errorMessage = t("errors.errorAddingToCart");
        try {
          const errorData = await response.json();
          if (errorData.message) {
            errorMessage = errorData.message;
          }
        } catch (error) {
          if (response.statusText) {
            errorMessage = response.statusText;
          }
        }
        throw new Error(errorMessage);
      }

      setMessage({ type: "success", text: t("plantDetails.addedToCart") });
      if (onItemAdded) {
        onItemAdded(quantity);
      }
      setTimeout(() => {
        onClose();
      }, 1500);
    } catch (err) {
      console.error("Error adding to cart:", err);
      setMessage({ type: "error", text: translateError(err.message, "errors.errorAddingToCart") });
    } finally {
      setAddingToCart(false);
    }
  };

return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content plant-details-modal" data-testid="plant-details-modal" onClick={(e) => e.stopPropagation()}>
        {loading ? (
          <p className="auth-error">{t("plantDetails.loading")}</p>
        ) : plantDetails ? (
          <>
            <button
              className="modal-close-button"
              onClick={onClose}
              aria-label={t("plantDetails.closeAria")}
            >
              X
            </button>
            <div className="plant-details-grid">
              <div className="plant-details-image">
                <img
                  src={plantDetails.image_url || "https://via.placeholder.com/400"}
                  alt={plantDetails.title}
                />
              </div>
              <div className="plant-details-info">
                <span className="auth-kicker">
                  {plantDetails.categoryName ? translateCategory(plantDetails.categoryName) : t("common.uncategorized")}
                </span>
                <h2>{plantDetails.title}</h2>
                <p className="plant-details-price">{formatCurrency(plantDetails.amount)}</p>

                <div className="plant-details-specs">
                  <div className="spec-item">
                    <dt>{t("common.labels.description")}</dt>
                    <dd>{plantDetails.description || t("plantDetails.noDescription")}</dd>
                  </div>
                  <div className="spec-item">
                    <dt>{t("plantDetails.stockAvailable")}</dt>
                    <dd>{t("plantDetails.units", { count: plantDetails.quantity })}</dd>
                  </div>
                  <div className="spec-item">
                    <dt>{t("common.labels.status")}</dt>
                    <dd>{translateItemStatus(plantDetails.status)}</dd>
                  </div>
                </div>

                {message && (
                  <div className={`auth-${message.type === "success" ? "notice" : "error"}`}>
                    {message.text}
                  </div>
                )}

                <div className="quantity-selector">
                  <label htmlFor="quantity">{t("plantDetails.quantityLabel")}</label>
                  <input
                    id="quantity"
                    data-testid="plant-quantity"
                    type="number"
                    min="1"
                    max={plantDetails.quantity}
                    value={quantity}
                    onChange={(e) => setQuantity(Math.max(1, parseInt(e.target.value, 10) || 1))}
                    disabled={addingToCart}
                  />
                </div>

                <button
                  className="primary-button"
                  data-testid="plant-add-to-cart"
                  onClick={handleAddToCart}
                  disabled={addingToCart}
                >
                  {addingToCart ? t("common.actions.adding") : t("common.actions.addToCart")}
                </button>
              </div>
            </div>

            <div style={{ padding: "0 32px 32px 32px" }}>
              <hr style={{ margin: "0 0 2rem 0", borderTop: "1px solid #d1d5db" }} />
              
              <div onClick={(e) => e.stopPropagation()}>
                <CommentSection 
                  targetId={plantId}  
                  targetType="item" 
                  currentUserId={userId}  
                />
              </div>
            </div>
            
          </>
        ) : (
          <p className="auth-error">{t("plantDetails.error")}</p>
        )}
      </div>
    </div>
  );
}
