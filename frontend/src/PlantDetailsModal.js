import React from "react";

export default function PlantDetailsModal({ plantId, onClose }) {
  const [plantDetails, setPlantDetails] = React.useState(null);
  const [loading, setLoading] = React.useState(false);

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
                  alt={plantDetails.name}
                />
              </div>
              <div className="plant-details-info">
                <span className="auth-kicker">{plantDetails.categoryName || "Uncategorized"}</span>
                <h2>{plantDetails.name}</h2>
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

                <button className="primary-button">Add to Cart</button>
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