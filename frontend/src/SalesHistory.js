import { useEffect, useState } from "react";
import "./History.css";
import { useI18n } from "./i18n/I18nContext";

export default function SalesHistory({ userId }) {
  const { t, formatCurrency, formatDate, translateError } = useI18n();
  const [sales, setSales] = useState([]);
  const [totalSales, setTotalSales] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (!userId) {
      return;
    }

    const fetchSales = async () => {
      try {
        setLoading(true);
        const response = await fetch(`/api/sales/seller/${userId}/total`);
        if (!response.ok) {
          throw new Error(t("errors.failedToFetchSalesHistory"));
        }
        const data = await response.json();
        setSales(data.sales || []);
        setTotalSales(data.totalSales || 0);
        setError(null);
      } catch (err) {
        console.error("Error loading sales:", err);
        setError(translateError(err.message, "errors.failedToFetchSalesHistory"));
        setSales([]);
        setTotalSales(0);
      } finally {
        setLoading(false);
      }
    };

    fetchSales();
  }, [t, translateError, userId]);

  const calculateMonthlyData = () => {
    const monthlyTotals = {};
    sales.forEach((sale) => {
      const date = new Date(sale.createdAt);
      const monthKey = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}`;
      monthlyTotals[monthKey] = (monthlyTotals[monthKey] || 0) + sale.totalPrice;
    });
    return monthlyTotals;
  };

  const calculateItemStats = () => {
    const itemStats = {};
    sales.forEach((sale) => {
      if (!itemStats[sale.itemName]) {
        itemStats[sale.itemName] = {
          name: sale.itemName,
          quantity: 0,
          revenue: 0,
        };
      }
      itemStats[sale.itemName].quantity += sale.quantity;
      itemStats[sale.itemName].revenue += sale.totalPrice;
    });
    return Object.values(itemStats).sort((a, b) => b.revenue - a.revenue);
  };

  const monthlyData = calculateMonthlyData();
  const itemStats = calculateItemStats();
  const maxAmount = Math.max(...Object.values(monthlyData), 1);

  if (loading) {
    return (
      <div className="history-container">
        <div className="history-loading">{t("salesHistory.loading")}</div>
      </div>
    );
  }

  return (
    <div className="history-container">
      <h2>{t("salesHistory.title")}</h2>

      {error && <div className="history-error">{error}</div>}

      {sales.length === 0 ? (
        <div className="history-empty">
          <p>{t("salesHistory.empty")}</p>
        </div>
      ) : (
        <>
          <div className="history-summary">
            <div className="summary-stat">
              <p className="stat-label">{t("salesHistory.totalRevenue")}</p>
              <p className="stat-value">{formatCurrency(totalSales)}</p>
            </div>
            <div className="summary-stat">
              <p className="stat-label">{t("salesHistory.totalTransactions")}</p>
              <p className="stat-value">{sales.length}</p>
            </div>
            <div className="summary-stat">
              <p className="stat-label">{t("salesHistory.averageSale")}</p>
              <p className="stat-value">{formatCurrency(totalSales / sales.length)}</p>
            </div>
            <div className="summary-stat">
              <p className="stat-label">{t("salesHistory.itemsSold")}</p>
              <p className="stat-value">{sales.reduce((sum, sale) => sum + sale.quantity, 0)}</p>
            </div>
          </div>

          <div className="history-chart">
            <h3>{t("salesHistory.monthlyRevenue")}</h3>
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

          <div className="top-items">
            <h3>{t("salesHistory.topSellingItems")}</h3>
            <div className="items-grid">
              {itemStats.map((item) => (
                <div key={item.name} className="item-stat-card">
                  <p className="item-name">{item.name}</p>
                  <p className="item-stat">
                    <span className="stat-label">{t("salesHistory.sold")}</span>
                    <span className="stat-value">{item.quantity}</span>
                  </p>
                  <p className="item-stat">
                    <span className="stat-label">{t("salesHistory.revenue")}</span>
                    <span className="stat-value">{formatCurrency(item.revenue)}</span>
                  </p>
                </div>
              ))}
            </div>
          </div>

          <div className="sales-list">
            <h3>{t("salesHistory.recentSales")}</h3>
            {sales.map((sale, index) => (
              <div key={index} className="sale-card">
                <div className="sale-card-header">
                  <div className="sale-info">
                    <p className="sale-item">{sale.itemName}</p>
                    <p className="sale-date">{formatDate(sale.createdAt, {
                      year: "numeric",
                      month: "short",
                      day: "numeric",
                    })}</p>
                  </div>
                  <div className="sale-amount">
                    <p className="sale-quantity">x{sale.quantity}</p>
                    <p className="sale-total">{formatCurrency(sale.totalPrice)}</p>
                  </div>
                </div>
                <div className="sale-card-footer">
                  <span className="unit-price">{t("salesHistory.unitPrice")} {formatCurrency(sale.unitPrice)}</span>
                </div>
              </div>
            ))}
          </div>
        </>
      )}
    </div>
  );
}
