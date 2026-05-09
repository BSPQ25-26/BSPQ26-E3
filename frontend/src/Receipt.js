import "./Checkout.css";
import { useI18n } from "./i18n/I18nContext";

export default function Receipt({ receipt, onClose }) {
  const { t, formatCurrency, formatDate } = useI18n();

  return (
    <div className="receipt-overlay">
      <div className="receipt-modal">
        <div className="receipt-header">
          <h2>{t("receipt.successTitle")}</h2>
          <button
            className="receipt-close"
            onClick={onClose}
            aria-label={t("receipt.closeAria")}
          >
            X
          </button>
        </div>

        <div className="receipt-content">
          <div className="receipt-number">
            <p className="label">{t("common.labels.receiptNumber")}:</p>
            <p className="value">{receipt.receiptNumber}</p>
          </div>

          <div className="receipt-date">
            <p className="label">{t("common.labels.dateTime")}:</p>
            <p className="value">{formatDate(receipt.createdAt, {
              year: "numeric",
              month: "long",
              day: "numeric",
              hour: "2-digit",
              minute: "2-digit",
            })}</p>
          </div>

          <div className="receipt-items">
            <h3>{t("receipt.orderItems")}</h3>
            <table className="receipt-table">
              <thead>
                <tr>
                  <th>{t("common.labels.item")}</th>
                  <th>{t("common.labels.qty")}</th>
                  <th>{t("common.labels.unitPrice")}</th>
                  <th>{t("common.labels.subtotal")}</th>
                </tr>
              </thead>
              <tbody>
                {receipt.items?.map((item) => (
                  <tr key={item.itemId}>
                    <td>{item.itemName}</td>
                    <td className="qty-col">{item.quantity}</td>
                    <td className="price-col">{formatCurrency(item.unitPrice)}</td>
                    <td className="price-col">{formatCurrency(item.subtotal)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <div className="receipt-summary">
            <div className="summary-row">
              <span className="label">{t("common.labels.subtotal")}:</span>
              <span className="value">{formatCurrency(receipt.totalAmount)}</span>
            </div>
            <div className="summary-row">
              <span className="label">{t("receipt.tax")}:</span>
              <span className="value">{formatCurrency(0)}</span>
            </div>
            <div className="summary-row total">
              <span className="label">{t("common.labels.total")}:</span>
              <span className="value">{formatCurrency(receipt.totalAmount)}</span>
            </div>
          </div>

          <div className="receipt-status">
            <p>{t("receipt.status")}: <strong className="status-badge">{receipt.paymentStatus}</strong></p>
          </div>

          <div className="receipt-notice">
            <p>{t("receipt.notice")}</p>
          </div>

          <button
            className="primary-button receipt-button"
            onClick={onClose}
          >
            {t("common.actions.continueShopping")}
          </button>
        </div>
      </div>
    </div>
  );
}
