import { useState } from "react";
import "./Checkout.css";
import { useI18n } from "./i18n/I18nContext";

export default function Checkout({ userId, cartTotal, onCheckoutSuccess, onCancel }) {
  const { t, formatCurrency, translateError } = useI18n();
  const [formData, setFormData] = useState({
    cardNumber: "",
    cardHolder: "",
    expiryDate: "",
    cvv: "",
  });

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    let formattedValue = value;

    if (name === "cardNumber") {
      formattedValue = value
        .replace(/\s/g, "")
        .replace(/(\d{4})/g, "$1 ")
        .trim();
    }

    if (name === "expiryDate") {
      formattedValue = value.replace(/\D/g, "");
      if (formattedValue.length >= 2) {
        formattedValue = formattedValue.slice(0, 2) + "/" + formattedValue.slice(2, 4);
      }
    }

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
      setError(t("errors.cardNumberInvalid"));
      return false;
    }
    if (!formData.cardHolder.trim()) {
      setError(t("errors.cardHolderRequired"));
      return false;
    }
    if (!formData.expiryDate || formData.expiryDate.length !== 5) {
      setError(t("errors.expiryInvalid"));
      return false;
    }
    if (!formData.cvv || formData.cvv.length < 3) {
      setError(t("errors.cvvInvalid"));
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
      const response = await fetch(`/api/carts/${userId}/checkout`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(formData),
      });

      if (!response.ok) {
        const errBody = await response.json().catch(() => ({}));
        throw new Error(errBody.message || t("errors.checkoutFailed"));
      }

      const receipt = await response.json();
      onCheckoutSuccess(receipt);
    } catch (err) {
      console.error("Error during checkout:", err);
      setError(t("errors.checkoutProcessing", { message: translateError(err.message, "errors.checkoutFailed") }));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="checkout-overlay">
      <div className="checkout-modal" data-testid="checkout-modal">
        <div className="checkout-header">
          <h2>{t("checkout.title")}</h2>
          <button
            className="checkout-close"
            onClick={onCancel}
            aria-label={t("checkout.closeAria")}
          >
            X
          </button>
        </div>

        <form className="checkout-form" onSubmit={handleSubmit}>
          <div className="checkout-amount">
            <p className="amount-label">{t("checkout.totalAmountToPay")}</p>
            <p className="amount-value">{formatCurrency(cartTotal)}</p>
          </div>

          {error && <div className="checkout-error">{error}</div>}

          <div className="form-group full-width">
            <label htmlFor="cardNumber">{t("common.labels.cardNumber")}</label>
            <input
              type="text"
              id="cardNumber"
              name="cardNumber"
              data-testid="checkout-card-number"
              placeholder="1234 5678 9012 3456"
              value={formData.cardNumber}
              onChange={handleInputChange}
              maxLength="23"
              disabled={loading}
              required
            />
          </div>

          <div className="form-group full-width">
            <label htmlFor="cardHolder">{t("common.labels.cardholderName")}</label>
            <input
              type="text"
              id="cardHolder"
              name="cardHolder"
              data-testid="checkout-card-holder"
              placeholder="Aretx Oca"
              value={formData.cardHolder}
              onChange={handleInputChange}
              disabled={loading}
              required
            />
          </div>

          <div className="form-row">
            <div className="form-group">
              <label htmlFor="expiryDate">{t("common.labels.expiryDate")}</label>
              <input
                type="text"
                id="expiryDate"
                name="expiryDate"
                data-testid="checkout-expiry"
                placeholder="MM/YY"
                value={formData.expiryDate}
                onChange={handleInputChange}
                maxLength="5"
                disabled={loading}
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="cvv">{t("common.labels.cvv")}</label>
              <input
                type="text"
                id="cvv"
                name="cvv"
                data-testid="checkout-cvv"
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
            <p>{t("checkout.simulatedNotice")}</p>
          </div>

          <div className="checkout-actions">
            <button
              type="button"
              className="secondary-button"
              onClick={onCancel}
              disabled={loading}
            >
              {t("common.actions.cancel")}
            </button>
            <button
              type="submit"
              className="primary-button"
              data-testid="checkout-submit"
              disabled={loading}
            >
              {loading ? t("common.actions.processing") : t("common.actions.completePayment")}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
