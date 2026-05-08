import "./Checkout.css";

export default function Receipt({ receipt, onClose }) {
  const formatDate = (dateString) => {
    if (!dateString) return "N/A";
    const date = new Date(dateString);
    return date.toLocaleString("en-US", {
      year: "numeric",
      month: "long",
      day: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  return (
    <div className="receipt-overlay">
      <div className="receipt-modal">
        <div className="receipt-header">
          <h2>✓ Payment Successful</h2>
          <button
            className="receipt-close"
            onClick={onClose}
            aria-label="Close receipt"
          >
            ✕
          </button>
        </div>

        <div className="receipt-content">
          <div className="receipt-number">
            <p className="label">Receipt Number:</p>
            <p className="value">{receipt.receiptNumber}</p>
          </div>

          <div className="receipt-date">
            <p className="label">Date & Time:</p>
            <p className="value">{formatDate(receipt.createdAt)}</p>
          </div>

          <div className="receipt-items">
            <h3>Order Items</h3>
            <table className="receipt-table">
              <thead>
                <tr>
                  <th>Item</th>
                  <th>Qty</th>
                  <th>Unit Price</th>
                  <th>Subtotal</th>
                </tr>
              </thead>
              <tbody>
                {receipt.items &&
                  receipt.items.map((item) => (
                    <tr key={item.itemId}>
                      <td>{item.itemName}</td>
                      <td className="qty-col">{item.quantity}</td>
                      <td className="price-col">${item.unitPrice.toFixed(2)}</td>
                      <td className="price-col">${item.subtotal.toFixed(2)}</td>
                    </tr>
                  ))}
              </tbody>
            </table>
          </div>

          <div className="receipt-summary">
            <div className="summary-row">
              <span className="label">Subtotal:</span>
              <span className="value">${receipt.totalAmount.toFixed(2)}</span>
            </div>
            <div className="summary-row">
              <span className="label">Tax (0%):</span>
              <span className="value">$0.00</span>
            </div>
            <div className="summary-row total">
              <span className="label">Total:</span>
              <span className="value">${receipt.totalAmount.toFixed(2)}</span>
            </div>
          </div>

          <div className="receipt-status">
            <p>Status: <strong className="status-badge">{receipt.paymentStatus}</strong></p>
          </div>

          <div className="receipt-notice">
            <p>Thank you for your purchase! Your order has been saved to your purchase history.</p>
          </div>

          <button
            className="primary-button receipt-button"
            onClick={onClose}
          >
            Continue Shopping
          </button>
        </div>
      </div>
    </div>
  );
}
