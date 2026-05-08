import { useState } from "react";
import "./Checkout.css";

export default function Checkout({ userId, cartTotal, onCheckoutSuccess, onCancel }) {
  const [formData, setFormData] = useState({
    cardNumber: "",
    cardHolder: "",
    expiryDate: "",
    cvv: "",
  });

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [showConfirmation, setShowConfirmation] = useState(false);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    let formattedValue = value;

    // Format card number (add spaces every 4 digits)
    if (name === "cardNumber") {
      formattedValue = value
        .replace(/\s/g, "")
        .replace(/(\d{4})/g, "$1 ")
        .trim();
    }

    // Format expiry date (MM/YY)
    if (name === "expiryDate") {
      formattedValue = value.replace(/\D/g, "");
      if (formattedValue.length >= 2) {
        formattedValue = formattedValue.slice(0, 2) + "/" + formattedValue.slice(2, 4);
      }
    }

    // Limit CVV to 4 digits
    if (name === "cvv") {
      formattedValue = value.replace(/\D/g, "").slice(0, 4);
    }

    setFormData((prev) => ({
      ...prev,
      [name]: formattedValue,
    }));
  };

  const validateForm = () => {
    if (!formData.cardNumber.replace(/\s/g, "") || formData.cardNumber.replace(/\s/g, "").length < 13) {
      setError("Card number must be at least 13 digits");
      return false;
    }
    if (!formData.cardHolder.trim()) {
      setError("Card holder name is required");
      return false;
    }
    if (!formData.expiryDate || formData.expiryDate.length !== 5) {
      setError("Expiry date must be in MM/YY format");
      return false;
    }
    if (!formData.cvv || formData.cvv.length < 3) {
      setError("CVV must be 3-4 digits");
      return false;
    }
    return true;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);

    if (!validateForm()) {
      return;
    }

    setLoading(true);

    try {
      const response = await fetch("/api/carts/" + userId + "/checkout", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(formData),
      });

      if (!response.ok) {
        const errBody = await response.json();
        throw new Error(errBody.message || "Checkout failed");
      }

      const receipt = await response.json();
      onCheckoutSuccess(receipt);
    } catch (err) {
      console.error("Error during checkout:", err);
      setError("Error processing checkout: " + err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="checkout-overlay">
      <div className="checkout-modal">
        <div className="checkout-header">
          <h2>Payment Information</h2>
          <button
            className="checkout-close"
            onClick={onCancel}
            aria-label="Close checkout"
          >
            ✕
          </button>
        </div>

        <form className="checkout-form" onSubmit={handleSubmit}>
          <div className="checkout-amount">
            <p className="amount-label">Total Amount to Pay:</p>
            <p className="amount-value">${cartTotal.toFixed(2)}</p>
          </div>

          {error && <div className="checkout-error">{error}</div>}

          <div className="form-group full-width">
            <label htmlFor="cardNumber">Card Number</label>
            <input
              type="text"
              id="cardNumber"
              name="cardNumber"
              placeholder="1234 5678 9012 3456"
              value={formData.cardNumber}
              onChange={handleInputChange}
              maxLength="23"
              disabled={loading}
              required
            />
          </div>

          <div className="form-group full-width">
            <label htmlFor="cardHolder">Cardholder Name</label>
            <input
              type="text"
              id="cardHolder"
              name="cardHolder"
              placeholder="Aretx Oca"
              value={formData.cardHolder}
              onChange={handleInputChange}
              disabled={loading}
              required
            />
          </div>

          <div className="form-row">
            <div className="form-group">
              <label htmlFor="expiryDate">Expiry Date</label>
              <input
                type="text"
                id="expiryDate"
                name="expiryDate"
                placeholder="MM/YY"
                value={formData.expiryDate}
                onChange={handleInputChange}
                maxLength="5"
                disabled={loading}
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="cvv">CVV</label>
              <input
                type="text"
                id="cvv"
                name="cvv"
                placeholder="123"
                value={formData.cvv}
                onChange={handleInputChange}
                maxLength="4"
                disabled={loading}
                required
              />
            </div>
          </div>

          <div className="checkout-notice">
            <p> This is a simulated payment system. No real charges will be made.</p>
          </div>

          <div className="checkout-actions">
            <button
              type="button"
              className="secondary-button"
              onClick={onCancel}
              disabled={loading}
            >
              Cancel
            </button>
            <button
              type="submit"
              className="primary-button"
              disabled={loading}
            >
              {loading ? "Processing..." : "Complete Payment"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
