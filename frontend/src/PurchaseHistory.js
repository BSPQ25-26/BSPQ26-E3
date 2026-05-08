import { useEffect, useState } from "react";
import "./History.css";

export default function PurchaseHistory({ userId }) {
  const [purchases, setPurchases] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedReceipt, setSelectedReceipt] = useState(null);
  const [pollingIntervals, setPollingIntervals] = useState({});
  const [cancellingId, setCancellingId] = useState(null);

  useEffect(() => {
    if (!userId) return;

    const fetchPurchases = async () => {
      try {
        setLoading(true);
        const response = await fetch(`/api/receipts/buyer/${userId}`);
        if (!response.ok) {
          throw new Error("Failed to fetch purchase history");
        }
        const data = await response.json();
        setPurchases(data || []);
        setError(null);
      } catch (err) {
        console.error("Error loading purchases:", err);
        setError(err.message);
        setPurchases([]);
      } finally {
        setLoading(false);
      }
    };

    fetchPurchases();
  }, [userId]);

  // Set up polling for active orders
  useEffect(() => {
    const activeOrders = purchases.filter(
      (p) => p.orderStatus === "PROCESSING" || p.orderStatus === "DELIVERY"
    );

    activeOrders.forEach((order) => {
      if (!pollingIntervals[order.receiptId]) {
        const interval = setInterval(async () => {
          try {
            const response = await fetch(`/api/receipts/${order.receiptId}`);
            if (response.ok) {
              const updatedOrder = await response.json();
              setPurchases((prev) =>
                prev.map((p) => (p.receiptId === order.receiptId ? updatedOrder : p))
              );
              if (selectedReceipt?.receiptId === order.receiptId) {
                setSelectedReceipt(updatedOrder);
              }
            }
          } catch (err) {
            console.error("Error updating order status:", err);
          }
        }, 2000); // Poll every 2 seconds

        setPollingIntervals((prev) => ({ ...prev, [order.receiptId]: interval }));
      }
    });

    // Clean up polling for completed/cancelled orders
    return () => {
      Object.entries(pollingIntervals).forEach(([receiptId, interval]) => {
        const order = purchases.find((p) => p.receiptId === parseInt(receiptId));
        if (
          order &&
          (order.orderStatus === "COMPLETED" || order.orderStatus === "CANCELLED")
        ) {
          clearInterval(interval);
          setPollingIntervals((prev) => {
            const newIntervals = { ...prev };
            delete newIntervals[receiptId];
            return newIntervals;
          });
        }
      });
    };
  }, [purchases, pollingIntervals, selectedReceipt]);

  // Cleanup intervals on
  useEffect(() => {
    return () => {
      Object.values(pollingIntervals).forEach((interval) => clearInterval(interval));
    };
  }, []);

  const formatDate = (dateString) => {
    if (!dateString) return "N/A";
    const date = new Date(dateString);
    return date.toLocaleDateString("en-US", {
      year: "numeric",
      month: "short",
      day: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  const getStatusColor = (status) => {
    switch (status) {
      case "PROCESSING":
        return "#FFA500"; // Orange
      case "DELIVERY":
        return "#4A90E2"; // Blue
      case "COMPLETED":
        return "#28a745"; // Green
      case "CANCELLED":
        return "#dc3545"; // Red
      default:
        return "#6c757d"; // Gray
    }
  };

  const getStatusLabel = (status) => {
    switch (status) {
      case "PROCESSING":
        return "Processing";
      case "DELIVERY":
        return "In Delivery";
      case "COMPLETED":
        return "Completed";
      case "CANCELLED":
        return "Cancelled";
      default:
        return status;
    }
  };

  const handleCancel = async (receiptId) => {
    if (!window.confirm("Are you sure you want to cancel this order?")) return;

    try {
      setCancellingId(receiptId);
      const response = await fetch(`/api/receipts/${receiptId}/cancel`, {
        method: "POST",
      });

      if (response.ok) {
        const updatedOrder = await response.json();
        setPurchases((prev) =>
          prev.map((p) => (p.receiptId === receiptId ? updatedOrder : p))
        );
        if (selectedReceipt?.receiptId === receiptId) {
          setSelectedReceipt(updatedOrder);
        }
      } else {
        const errorData = await response.json();
        alert("Failed to cancel order: " + errorData.message);
      }
    } catch (err) {
      console.error("Error cancelling order:", err);
      alert("Error cancelling order: " + err.message);
    } finally {
      setCancellingId(null);
    }
  };

  const calculateMonthlyData = () => {
    const monthlyTotals = {};
    purchases.forEach((purchase) => {
      const date = new Date(purchase.createdAt);
      const monthKey = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}`;
      monthlyTotals[monthKey] = (monthlyTotals[monthKey] || 0) + purchase.totalAmount;
    });
    return monthlyTotals;
  };

  const monthlyData = calculateMonthlyData();
  const maxAmount = Math.max(...Object.values(monthlyData), 1);

  if (loading) {
    return (
      <div className="history-container">
        <div className="history-loading">Loading purchase history...</div>
      </div>
    );
  }

  return (
    <div className="history-container">
      <h2>Your Purchase History</h2>

      {error && <div className="history-error">{error}</div>}

      {purchases.length === 0 ? (
        <div className="history-empty">
          <p>No purchases yet. Start shopping to see your purchase history here!</p>
        </div>
      ) : (
        <>
          {/* Chart */}
          <div className="history-chart">
            <h3>Monthly Spending</h3>
            <div className="chart-bars">
              {Object.entries(monthlyData).map(([month, amount]) => (
                <div key={month} className="chart-bar-group">
                  <div className="chart-bar-container">
                    <div
                      className="chart-bar"
                      style={{
                        height: `${(amount / maxAmount) * 200}px`,
                      }}
                      title={`$${amount.toFixed(2)}`}
                    ></div>
                  </div>
                  <span className="chart-label">{month}</span>
                  <span className="chart-value">${amount.toFixed(2)}</span>
                </div>
              ))}
            </div>
          </div>

          {/* Total Summary */}
          <div className="history-summary">
            <div className="summary-stat">
              <p className="stat-label">Total Spent</p>
              <p className="stat-value">
                ${purchases.reduce((sum, p) => sum + p.totalAmount, 0).toFixed(2)}
              </p>
            </div>
            <div className="summary-stat">
              <p className="stat-label">Total Orders</p>
              <p className="stat-value">{purchases.length}</p>
            </div>
            <div className="summary-stat">
              <p className="stat-label">Average Order</p>
              <p className="stat-value">
                ${(purchases.reduce((sum, p) => sum + p.totalAmount, 0) / purchases.length).toFixed(2)}
              </p>
            </div>
          </div>

          {/* Receipts List */}
          <div className="receipts-list">
            <h3>Recent Orders</h3>
            {purchases.map((purchase) => (
              <div key={purchase.receiptId} className="receipt-card">
                <div className="receipt-card-header">
                  <div className="receipt-info">
                    <p className="receipt-number">{purchase.receiptNumber}</p>
                    <p className="receipt-date">{formatDate(purchase.createdAt)}</p>
                  </div>
                  <div className="receipt-amount">
                    <p className="receipt-total">${purchase.totalAmount.toFixed(2)}</p>
                    <span
                      className="status-badge"
                      style={{
                        backgroundColor: getStatusColor(purchase.orderStatus),
                        color: "white",
                      }}
                    >
                      {getStatusLabel(purchase.orderStatus)}
                    </span>
                  </div>
                </div>
                <div className="receipt-card-items">
                  <p className="items-count">{purchase.items?.length || 0} items</p>
                  {purchase.items?.slice(0, 2).map((item) => (
                    <p key={item.itemId} className="item-name">
                      • {item.itemName} (x{item.quantity})
                    </p>
                  ))}
                  {purchase.items?.length > 2 && (
                    <p className="more-items">+ {purchase.items.length - 2} more</p>
                  )}
                </div>
                <div className="receipt-card-actions">
                  {purchase.remainingTimeSeconds > 0 && (
                    <p className="remaining-time">
                       {purchase.remainingTimeSeconds}s remaining
                    </p>
                  )}
                  <button
                    className="secondary-button view-receipt-btn"
                    onClick={() => setSelectedReceipt(purchase)}
                  >
                    View Details
                  </button>
                  {purchase.orderStatus === "PROCESSING" && (
                    <button
                      className="danger-button"
                      onClick={() => handleCancel(purchase.receiptId)}
                      disabled={cancellingId === purchase.receiptId}
                    >
                      {cancellingId === purchase.receiptId ? "Cancelling..." : "Cancel Order"}
                    </button>
                  )}
                </div>
              </div>
            ))}
          </div>

          {/* Receipt Details Modal */}
          {selectedReceipt && (
            <div className="modal-overlay" onClick={() => setSelectedReceipt(null)}>
              <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                <div className="modal-header">
                  <h3>Receipt Details</h3>
                  <button
                    className="modal-close"
                    onClick={() => setSelectedReceipt(null)}
                  >
                    ✕
                  </button>
                </div>
                <div className="modal-body">
                  <div className="detail-section">
                    <p>
                      <strong>Receipt #:</strong> {selectedReceipt.receiptNumber}
                    </p>
                    <p>
                      <strong>Date:</strong> {formatDate(selectedReceipt.createdAt)}
                    </p>
                    <p>
                      <strong>Order Status:</strong>{" "}
                      <span
                        style={{
                          color: getStatusColor(selectedReceipt.orderStatus),
                          fontWeight: "bold",
                        }}
                      >
                        {getStatusLabel(selectedReceipt.orderStatus)}
                      </span>
                    </p>
                    {selectedReceipt.remainingTimeSeconds > 0 && (
                      <p>
                        <strong>Time Remaining:</strong> {selectedReceipt.remainingTimeSeconds}s
                      </p>
                    )}
                  </div>
                  <table className="detail-table">
                    <thead>
                      <tr>
                        <th>Item</th>
                        <th>Qty</th>
                        <th>Price</th>
                        <th>Subtotal</th>
                      </tr>
                    </thead>
                    <tbody>
                      {selectedReceipt.items?.map((item) => (
                        <tr key={item.itemId}>
                          <td>{item.itemName}</td>
                          <td>{item.quantity}</td>
                          <td>${item.unitPrice.toFixed(2)}</td>
                          <td>${(item.unitPrice * item.quantity).toFixed(2)}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                  <div className="detail-total">
                    <p>
                      <strong>Total:</strong> ${selectedReceipt.totalAmount.toFixed(2)}
                    </p>
                  </div>
                  {selectedReceipt.orderStatus === "PROCESSING" && (
                    <div className="modal-actions">
                      <button
                        className="danger-button"
                        onClick={() => {
                          handleCancel(selectedReceipt.receiptId);
                          setSelectedReceipt(null);
                        }}
                        disabled={cancellingId === selectedReceipt.receiptId}
                      >
                        {cancellingId === selectedReceipt.receiptId
                          ? "Cancelling..."
                          : "Cancel Order"}
                      </button>
                    </div>
                  )}
                </div>
              </div>
            </div>
          )}
        </>
      )}
    </div>
  );
}
