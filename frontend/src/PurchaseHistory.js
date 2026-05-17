import { useEffect, useState } from "react";
import "./History.css";
import { useI18n } from "./i18n/I18nContext";

export default function PurchaseHistory({ userId }) {
  const { t, formatCurrency, formatDate, translateError, translateOrderStatus } = useI18n();
  const [purchases, setPurchases] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedReceipt, setSelectedReceipt] = useState(null);
  const [pollingIntervals, setPollingIntervals] = useState({});
  const [cancellingId, setCancellingId] = useState(null);

  useEffect(() => {
    if (!userId) {
      return;
    }

    const fetchPurchases = async () => {
      try {
        setLoading(true);
        const response = await fetch(`/api/receipts/buyer/${userId}`);
        if (!response.ok) {
          throw new Error(t("errors.failedToFetchPurchaseHistory"));
        }
        const data = await response.json();
        setPurchases(data || []);
        setError(null);
      } catch (err) {
        console.error("Error loading purchases:", err);
        setError(translateError(err.message, "errors.failedToFetchPurchaseHistory"));
        setPurchases([]);
      } finally {
        setLoading(false);
      }
    };

    fetchPurchases();
  }, [t, translateError, userId]);

  useEffect(() => {
    const activeOrders = purchases.filter(
      (purchase) => purchase.orderStatus === "PROCESSING" || purchase.orderStatus === "DELIVERY"
    );

    activeOrders.forEach((order) => {
      if (!pollingIntervals[order.receiptId]) {
        const interval = setInterval(async () => {
          try {
            const response = await fetch(`/api/receipts/${order.receiptId}`);
            if (response.ok) {
              const updatedOrder = await response.json();
              setPurchases((prev) =>
                prev.map((purchase) => (purchase.receiptId === order.receiptId ? updatedOrder : purchase))
              );
              if (selectedReceipt?.receiptId === order.receiptId) {
                setSelectedReceipt(updatedOrder);
              }
            }
          } catch (err) {
            console.error("Error updating order status:", err);
          }
        }, 2000);

        setPollingIntervals((prev) => ({ ...prev, [order.receiptId]: interval }));
      }
    });

    return () => {
      Object.entries(pollingIntervals).forEach(([receiptId, interval]) => {
        const order = purchases.find((purchase) => purchase.receiptId === parseInt(receiptId, 10));
        if (order && (order.orderStatus === "COMPLETED" || order.orderStatus === "CANCELLED")) {
          clearInterval(interval);
          setPollingIntervals((prev) => {
            const nextIntervals = { ...prev };
            delete nextIntervals[receiptId];
            return nextIntervals;
          });
        }
      });
    };
  }, [pollingIntervals, purchases, selectedReceipt]);

  useEffect(() => {
    return () => {
      Object.values(pollingIntervals).forEach((interval) => clearInterval(interval));
    };
  }, [pollingIntervals]);
  const getStatusColor = (status) => {
    switch (status) {
      case "PROCESSING":
        return "#FFA500";
      case "DELIVERY":
        return "#4A90E2";
      case "COMPLETED":
        return "#28a745";
      case "CANCELLED":
        return "#dc3545";
      default:
        return "#6c757d";
    }
  };

  const handleCancel = async (receiptId) => {
    if (!window.confirm(t("purchaseHistory.confirmCancel"))) {
      return;
    }

    try {
      setCancellingId(receiptId);
      const response = await fetch(`/api/receipts/${receiptId}/cancel`, {
        method: "POST",
      });

      if (response.ok) {
        const updatedOrder = await response.json();
        setPurchases((prev) =>
          prev.map((purchase) => (purchase.receiptId === receiptId ? updatedOrder : purchase))
        );
        if (selectedReceipt?.receiptId === receiptId) {
          setSelectedReceipt(updatedOrder);
        }
      } else {
        const errorData = await response.json().catch(() => ({}));
        alert(t("purchaseHistory.failedToCancel", {
          message: translateError(errorData.message, "errors.generic"),
        }));
      }
    } catch (err) {
      console.error("Error cancelling order:", err);
      alert(t("purchaseHistory.errorCancelling", {
        message: translateError(err.message, "errors.generic"),
      }));
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
        <div className="history-loading">{t("purchaseHistory.loading")}</div>
      </div>
    );
  }

  return (
    <div className="history-container">
      <h2>{t("purchaseHistory.title")}</h2>

      {error && <div className="history-error">{error}</div>}

      {purchases.length === 0 ? (
        <div className="history-empty">
          <p>{t("purchaseHistory.empty")}</p>
        </div>
      ) : (
        <>
          <div className="history-chart">
            <h3>{t("purchaseHistory.monthlySpending")}</h3>
            <div className="chart-bars">
              {Object.entries(monthlyData).map(([month, amount]) => (
                <div key={month} className="chart-bar-group">
                  <div className="chart-bar-container">
                    <div
                      className="chart-bar"
                      style={{ height: `${(amount / maxAmount) * 200}px` }}
                      title={formatCurrency(amount)}
                    ></div>
                  </div>
                  <span className="chart-label">{month}</span>
                  <span className="chart-value">{formatCurrency(amount)}</span>
                </div>
              ))}
            </div>
          </div>

          <div className="history-summary">
            <div className="summary-stat">
              <p className="stat-label">{t("purchaseHistory.totalSpent")}</p>
              <p className="stat-value">{formatCurrency(purchases.reduce((sum, purchase) => sum + purchase.totalAmount, 0))}</p>
            </div>
            <div className="summary-stat">
              <p className="stat-label">{t("purchaseHistory.totalOrders")}</p>
              <p className="stat-value">{purchases.length}</p>
            </div>
            <div className="summary-stat">
              <p className="stat-label">{t("purchaseHistory.averageOrder")}</p>
              <p className="stat-value">{formatCurrency(purchases.reduce((sum, purchase) => sum + purchase.totalAmount, 0) / purchases.length)}</p>
            </div>
          </div>

          <div className="receipts-list">
            <h3>{t("purchaseHistory.recentOrders")}</h3>
            {purchases.map((purchase) => (
              <div key={purchase.receiptId} className="receipt-card">
                <div className="receipt-card-header">
                  <div className="receipt-info">
                    <p className="receipt-number">{purchase.receiptNumber}</p>
                    <p className="receipt-date">{formatDate(purchase.createdAt, {
                      year: "numeric",
                      month: "short",
                      day: "numeric",
                      hour: "2-digit",
                      minute: "2-digit",
                    })}</p>
                  </div>
                  <div className="receipt-amount">
                    <p className="receipt-total">{formatCurrency(purchase.totalAmount)}</p>
                    <span
                      className="status-badge"
                      style={{ backgroundColor: getStatusColor(purchase.orderStatus), color: "white" }}
                    >
                      {translateOrderStatus(purchase.orderStatus)}
                    </span>
                  </div>
                </div>
                <div className="receipt-card-items">
                  <p className="items-count">{t("purchaseHistory.itemsCount", { count: purchase.items?.length || 0 })}</p>
                  {purchase.items?.slice(0, 2).map((item) => (
                    <p key={item.itemId} className="item-name">
                      - {item.itemName} (x{item.quantity})
                    </p>
                  ))}
                  {purchase.items?.length > 2 && (
                    <p className="more-items">{t("purchaseHistory.moreItems", { count: purchase.items.length - 2 })}</p>
                  )}
                </div>
                <div className="receipt-card-actions">
                  {purchase.remainingTimeSeconds > 0 && (
                    <p className="remaining-time">{t("purchaseHistory.remainingTime", { count: purchase.remainingTimeSeconds })}</p>
                  )}
                  <button
                    className="secondary-button view-receipt-btn"
                    onClick={() => setSelectedReceipt(purchase)}
                  >
                    {t("purchaseHistory.viewDetails")}
                  </button>
                  {purchase.orderStatus === "PROCESSING" && (
                    <button
                      className="danger-button"
                      onClick={() => handleCancel(purchase.receiptId)}
                      disabled={cancellingId === purchase.receiptId}
                    >
                      {cancellingId === purchase.receiptId ? t("purchaseHistory.cancelling") : t("purchaseHistory.cancelOrder")}
                    </button>
                  )}
                </div>
              </div>
            ))}
          </div>

          {selectedReceipt && (
            <div className="modal-overlay" onClick={() => setSelectedReceipt(null)}>
              <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                <div className="modal-header">
                  <h3>{t("purchaseHistory.receiptDetails")}</h3>
                  <button className="modal-close" onClick={() => setSelectedReceipt(null)}>
                    X
                  </button>
                </div>
                <div className="modal-body">
                  <div className="detail-section">
                    <p>
                      <strong>{t("common.labels.receiptNumber")}:</strong> {selectedReceipt.receiptNumber}
                    </p>
                    <p>
                      <strong>{t("common.labels.date")}:</strong> {formatDate(selectedReceipt.createdAt, {
                        year: "numeric",
                        month: "short",
                        day: "numeric",
                        hour: "2-digit",
                        minute: "2-digit",
                      })}
                    </p>
                    <p>
                      <strong>{t("common.labels.orderStatus")}:</strong>{" "}
                      <span style={{ color: getStatusColor(selectedReceipt.orderStatus), fontWeight: "bold" }}>
                        {translateOrderStatus(selectedReceipt.orderStatus)}
                      </span>
                    </p>
                    {selectedReceipt.remainingTimeSeconds > 0 && (
                      <p>
                        <strong>{t("common.labels.timeRemaining")}:</strong> {selectedReceipt.remainingTimeSeconds}s
                      </p>
                    )}
                  </div>
                  <table className="detail-table">
                    <thead>
                      <tr>
                        <th>{t("common.labels.item")}</th>
                        <th>{t("common.labels.qty")}</th>
                        <th>{t("common.labels.price")}</th>
                        <th>{t("common.labels.subtotal")}</th>
                      </tr>
                    </thead>
                    <tbody>
                      {selectedReceipt.items?.map((item) => (
                        <tr key={item.itemId}>
                          <td>{item.itemName}</td>
                          <td>{item.quantity}</td>
                          <td>{formatCurrency(item.unitPrice)}</td>
                          <td>{formatCurrency(item.unitPrice * item.quantity)}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                  <div className="detail-total">
                    <p>
                      <strong>{t("common.labels.total")}:</strong> {formatCurrency(selectedReceipt.totalAmount)}
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
                        {cancellingId === selectedReceipt.receiptId ? t("purchaseHistory.cancelling") : t("purchaseHistory.cancelOrder")}
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
