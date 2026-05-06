import { useEffect, useState } from "react";
import emptyCartImg from "./assets/shopping-cart.png";
import fullCartImg from "./assets/shopping-cart-filled.png";

export default function Cart({ userId, onClose, onCartLoad }) {
  const [cartData, setCartData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [updating, setUpdating] = useState(false);

  // Cargar carrito
  useEffect(() => {
    if (!userId) return;
    
    setLoading(true);
    setError(null);

    fetch(`/api/carts/${userId}`)
      .then(async (response) => {
        if (!response.ok) {
          throw new Error("Failed to fetch cart");
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
        setError(err.message);
        setLoading(false);
      });
  }, [userId, onCartLoad]);

  const handleRemoveItem = async (itemId) => {
    setUpdating(true);
    try {
      const response = await fetch(`/api/carts/${userId}/items/${itemId}`, {
        method: 'DELETE'
      });

      if (!response.ok) {
        throw new Error("Failed to remove item");
      }

      const updatedCart = await response.json();
      setCartData(updatedCart);
      if (onCartLoad && updatedCart?.items) {
        onCartLoad(updatedCart.items.reduce((sum, item) => sum + item.quantity, 0));
      }
    } catch (err) {
      console.error("Error removing item:", err);
      setError("Error removing item from cart");
    } finally {
      setUpdating(false);
    }
  };

  const handleCheckout = async () => {
    setUpdating(true);
    try {
      const response = await fetch(`/api/carts/${userId}/checkout`, {
        method: 'POST'
      });

      if (!response.ok) {
        throw new Error("Failed to checkout");
      }

      const updatedCart = await response.json();
      setCartData(updatedCart);
      if (onCartLoad) onCartLoad(0);
      alert("Checkout successful!");
      onClose();
    } catch (err) {
      console.error("Error during checkout:", err);
      setError("Error during checkout: " + err.message);
    } finally {
      setUpdating(false);
    }
  };

  const isEmpty = !cartData || !cartData.items || cartData.items.length === 0;

  return (
    <aside className="cart-sidebar">
      <div className="cart-header">
        <h2>Shopping Cart</h2>
        <button
          className="cart-close-button"
          onClick={onClose}
          aria-label="Close cart"
        >
          ✕
        </button>
      </div>

      {loading ? (
        <div className="cart-content">
          <p className="auth-error">Loading cart...</p>
        </div>
      ) : error ? (
        <div className="cart-content">
          <p className="auth-error">Error: {error}</p>
        </div>
      ) : isEmpty ? (
        <div className="cart-empty">
          <img src={emptyCartImg} alt="Empty cart" className="cart-state-img" />
          <h3>Your cart is empty</h3>
          <p>Start shopping to add items to your cart!</p>
          <button
            className="secondary-button"
            onClick={onClose}
          >
            Continue Shopping
          </button>
        </div>
      ) : (
        <>
          <div className="cart-full-header">
            <img src={fullCartImg} alt="Cart with items" className="cart-state-img cart-state-img--small" />
            <span className="cart-full-label">{cartData.items.reduce((sum, item) => sum + item.quantity, 0)} item{cartData.items.reduce((sum, item) => sum + item.quantity, 0) !== 1 ? 's' : ''} in your cart</span>
          </div>
          <div className="cart-items-container">
            {cartData.items.map((item) => (
              <div key={item.itemId} className="cart-item">
                <div className="cart-item-info">
                  <h4>{item.title}</h4>
                  <p className="cart-item-price">${item.amount.toFixed(2)}</p>
                </div>
                <div className="cart-item-quantity">
                  <span className="quantity-badge">{item.quantity}x</span>
                </div>
                <button
                  className="cart-remove-button"
                  onClick={() => handleRemoveItem(item.itemId)}
                  disabled={updating}
                  aria-label="Remove item"
                >
                  🗑️
                </button>
              </div>
            ))}
          </div>

          <div className="cart-summary">
            <div className="summary-row">
              <span>Subtotal:</span>
              <span>${cartData.total.toFixed(2)}</span>
            </div>
            <div className="summary-row">
              <span>Items:</span>
              <span>{cartData.items.reduce((sum, item) => sum + item.quantity, 0)}</span>
            </div>
            <div className="summary-total">
              <span>Total:</span>
              <span>${cartData.total.toFixed(2)}</span>
            </div>
          </div>

          <button 
            className="primary-button cart-checkout-button"
            onClick={handleCheckout}
            disabled={updating}
          >
            {updating ? 'Processing...' : 'Proceed to Checkout'}
          </button>
        </>
      )}
    </aside>
  );
}