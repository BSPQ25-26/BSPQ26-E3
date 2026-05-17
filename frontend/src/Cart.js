import { useEffect, useState } from "react";
import emptyCartImg from "./assets/shopping-cart.png";
import fullCartImg from "./assets/shopping-cart-filled.png";
import Checkout from "./Checkout";
import Receipt from "./Receipt";
import { useI18n } from "./i18n/I18nContext";

export default function Cart({ userId, onClose, onCartLoad }) {
  const { t, formatCurrency, translateError } = useI18n();
  const [cartData, setCartData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [updating, setUpdating] = useState(false);
  const [showCheckout, setShowCheckout] = useState(false);
  const [receipt, setReceipt] = useState(null);

  useEffect(() => {
    if (!userId) {
      return;
    }

    setLoading(true);
    setError(null);

    fetch(`/api/carts/${userId}`)
      .then(async (response) => {
        if (!response.ok) {
          throw new Error(t("errors.failedToFetchCart"));
        }
        return response.json();
      })
      .then((data) => {
        setCartData(data);
        setLoading(false);
        if (onCartLoad && data?.items) {
          onCartLoad(data.items.reduce((sum, item) => sum + item.quantity, 0));
        }
      })
      .catch((err) => {
        console.error("Error loading cart:", err);
        setError(translateError(err.message, "errors.failedToFetchCart"));
        setLoading(false);
      });
  }, [onCartLoad, t, translateError, userId]);

  const handleRemoveItem = async (itemId) => {
    setUpdating(true);
    try {
      const response = await fetch(`/api/carts/${userId}/items/${itemId}`, {
        method: "DELETE",
      });

      if (!response.ok) {
        throw new Error(t("errors.failedToRemoveItem"));
      }

      const updatedCart = await response.json();
      setCartData(updatedCart);
      if (onCartLoad && updatedCart?.items) {
        onCartLoad(updatedCart.items.reduce((sum, item) => sum + item.quantity, 0));
      }
    } catch (err) {
      console.error("Error removing item:", err);
      setError(translateError(err.message, "errors.errorRemovingItem"));
    } finally {
      setUpdating(false);
    }
  };

  const handleCheckoutSuccess = (receiptData) => {
    setReceipt(receiptData);
    setCartData(null);
    if (onCartLoad) {
      onCartLoad(0);
    }
    setShowCheckout(false);
  };

  const handleReceiptClose = () => {
    setReceipt(null);
    onClose();
  };

  const itemCount = cartData?.items?.reduce((sum, item) => sum + item.quantity, 0) ?? 0;
  const isEmpty = !cartData || !cartData.items || cartData.items.length === 0;

  return (
    <>
      <aside className="cart-sidebar" data-testid="cart-sidebar">
        <div className="cart-header">
          <h2>{t("cart.title")}</h2>
          <button
            className="cart-close-button"
            onClick={onClose}
            aria-label={t("cart.closeAria")}
          >
            X
          </button>
        </div>

        {loading ? (
          <div className="cart-content">
            <p className="auth-error">{t("cart.loading")}</p>
          </div>
        ) : error ? (
          <div className="cart-content">
            <p className="auth-error">Error: {error}</p>
          </div>
        ) : isEmpty ? (
          <div className="cart-empty">
            <img src={emptyCartImg} alt={t("cart.emptyAlt")} className="cart-state-img" />
            <h3>{t("cart.emptyTitle")}</h3>
            <p>{t("cart.emptyCopy")}</p>
            <button
              className="secondary-button"
              onClick={onClose}
            >
              {t("common.actions.continueShopping")}
            </button>
          </div>
        ) : (
          <>
            <div className="cart-full-header">
              <img src={fullCartImg} alt={t("cart.fullAlt")} className="cart-state-img cart-state-img--small" />
              <span className="cart-full-label">
                {itemCount === 1 ? t("cart.itemCountSingle", { count: itemCount }) : t("cart.itemCountPlural", { count: itemCount })}
              </span>
            </div>
            <div className="cart-items-container">
              {cartData.items.map((item) => (
                <div key={item.itemId} className="cart-item" data-testid="cart-item">
                  <div className="cart-item-info">
                    <h4 data-testid="cart-item-title">{item.title}</h4>
                    <p className="cart-item-price">{formatCurrency(item.amount)}</p>
                  </div>
                  <div className="cart-item-quantity">
                    <span className="quantity-badge">{item.quantity}x</span>
                  </div>
                  <button
                    className="cart-remove-button"
                    onClick={() => handleRemoveItem(item.itemId)}
                    disabled={updating}
                    aria-label={t("cart.removeAria")}
                  >
                    X
                  </button>
                </div>
              ))}
            </div>

            <div className="cart-summary">
              <div className="summary-row">
                <span>{t("common.labels.subtotal")}:</span>
                <span>{formatCurrency(cartData.total)}</span>
              </div>
              <div className="summary-row">
                <span>{t("common.labels.items")}:</span>
                <span>{itemCount}</span>
              </div>
              <div className="summary-total">
                <span>{t("common.labels.total")}:</span>
                <span>{formatCurrency(cartData.total)}</span>
              </div>
            </div>

            <button
              className="primary-button cart-checkout-button"
              data-testid="cart-checkout"
              onClick={() => setShowCheckout(true)}
              disabled={updating}
            >
              {updating ? t("common.actions.processing") : t("common.actions.proceedToCheckout")}
            </button>
          </>
        )}
      </aside>

      {showCheckout && (
        <Checkout
          userId={userId}
          cartTotal={cartData?.total || 0}
          onCheckoutSuccess={handleCheckoutSuccess}
          onCancel={() => setShowCheckout(false)}
        />
      )}

      {receipt && (
        <Receipt
          receipt={receipt}
          onClose={handleReceiptClose}
        />
      )}
    </>
  );
}
