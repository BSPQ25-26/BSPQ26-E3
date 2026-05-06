import React from "react";

export default function PlantDetailsModal({ plantId, userId, onClose, onItemAdded }) {
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
          throw new Error("Failed to fetch plant details");
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
  }, [plantId]);

  const handleAddToCart = async () => {
    if (!userId) {
      setMessage({ type: 'error', text: 'User ID not found' });
      return;
    }

    setAddingToCart(true);
    setMessage(null);

    try {
      const response = await fetch(`/api/carts/${userId}/items`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          itemId: plantId,
          quantity: quantity
        })
      });

      if (!response.ok) {
        let errorMessage = 'Error adding to cart';
        try {
          const errorData = await response.json();
          if (errorData.message) {
            errorMessage = errorData.message;
          }
        } catch (e) {
          // If response isn't JSON, use the status text
          if (response.statusText) {
            errorMessage = response.statusText;
          }
        }
        throw new Error(errorMessage);
      }

      setMessage({ type: 'success', text: 'Added to cart successfully!' });
      if (onItemAdded) onItemAdded(quantity);
      setTimeout(() => {
        onClose();
      }, 1500);
    } catch (err) {
      console.error('Error adding to cart:', err);
      let displayError = err.message || 'Error adding to cart';
      
      // User-friendly error messages
      if (displayError.includes('Not enough stock')) {
        displayError = 'Not enough stock available. Please reduce the quantity.';
      } else if (displayError.includes('not available')) {
        displayError = 'This item is no longer available for purchase.';
      } else if (displayError.includes('Quantity must be')) {
        displayError = 'Please select a valid quantity.';
      }
      
      setMessage({ type: 'error', text: displayError });
    } finally {
      setAddingToCart(false);
    }
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content plant-details-modal" onClick={(e) => e.stopPropagation()}>
        {loading ? (
          <p className="auth-error">Cargando detalles...</p>
        ) : plantDetails ? (
          <>
            <button 
              className="modal-close-button" 
              onClick={onClose}
              aria-label="Close details"
            >
              ✕
            </button>
            <div className="plant-details-grid">
              <div className="plant-details-image">
                <img
                  src={plantDetails.image_url || "https://via.placeholder.com/400"}
                  alt={plantDetails.title}
                />
              </div>
              <div className="plant-details-info">
                <span className="auth-kicker">{plantDetails.categoryName || "Uncategorized"}</span>
                <h2>{plantDetails.title}</h2>
                <p className="plant-details-price">${plantDetails.amount.toFixed(2)}</p>
                
                <div className="plant-details-specs">
                  <div className="spec-item">
                    <dt>Description</dt>
                    <dd>{plantDetails.description || "No description available"}</dd>
                  </div>
                  <div className="spec-item">
                    <dt>Stock Available</dt>
                    <dd>{plantDetails.quantity} units</dd>
                  </div>
                  <div className="spec-item">
                    <dt>Status</dt>
                    <dd>{plantDetails.status ? "Active" : "Inactive"}</dd>
                  </div>
                </div>

                {message && (
                  <div className={`auth-${message.type === 'success' ? 'notice' : 'error'}`}>
                    {message.text}
                  </div>
                )}

                <div className="quantity-selector">
                  <label htmlFor="quantity">Quantity:</label>
                  <input 
                    id="quantity"
                    type="number" 
                    min="1" 
                    max={plantDetails.quantity}
                    value={quantity}
                    onChange={(e) => setQuantity(Math.max(1, parseInt(e.target.value) || 1))}
                    disabled={addingToCart}
                  />
                </div>

                <button 
                  className="primary-button"
                  onClick={handleAddToCart}
                  disabled={addingToCart}
                >
                  {addingToCart ? 'Adding...' : 'Add to Cart'}
                </button>
              </div>
            </div>
          </>
        ) : (
          <p className="auth-error">Error loading plant details</p>
        )}
      </div>
    </div>
  );
}