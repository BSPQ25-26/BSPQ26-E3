import { useEffect, useState } from "react";
import "./History.css";

export default function SalesHistory({ userId }) {
  const [sales, setSales] = useState([]);
  const [totalSales, setTotalSales] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (!userId) return;

    const fetchSales = async () => {
      try {
        setLoading(true);
        const response = await fetch(`/api/sales/seller/${userId}/total`);
        if (!response.ok) {
          throw new Error("Failed to fetch sales history");
        }
        const data = await response.json();
        setSales(data.sales || []);
        setTotalSales(data.totalSales || 0);
        setError(null);
      } catch (err) {
        console.error("Error loading sales:", err);
        setError(err.message);
        setSales([]);
        setTotalSales(0);
      } finally {
        setLoading(false);
      }
    };

    fetchSales();
  }, [userId]);

  const formatDate = (dateString) => {
    if (!dateString) return "N/A";
    const date = new Date(dateString);
    return date.toLocaleDateString("en-US", {
      year: "numeric",
      month: "short",
      day: "numeric",
    });
  };

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
        <div className="history-loading">Loading sales history...</div>
      </div>
    );
  }

  return (
    <div className="history-container">
      <h2>Your Sales History</h2>

      {error && <div className="history-error">{error}</div>}

      {sales.length === 0 ? (
        <div className="history-empty">
          <p>No sales yet. Start selling to see your sales history here!</p>
        </div>
      ) : (
        <>
          {/* Sales Summary Stats */}
          <div className="history-summary">
            <div className="summary-stat">
              <p className="stat-label">Total Revenue</p>
              <p className="stat-value">${totalSales.toFixed(2)}</p>
            </div>
            <div className="summary-stat">
              <p className="stat-label">Total Transactions</p>
              <p className="stat-value">{sales.length}</p>
            </div>
            <div className="summary-stat">
              <p className="stat-label">Average Sale</p>
              <p className="stat-value">
                ${(totalSales / sales.length).toFixed(2)}
              </p>
            </div>
            <div className="summary-stat">
              <p className="stat-label">Items Sold</p>
              <p className="stat-value">
                {sales.reduce((sum, s) => sum + s.quantity, 0)}
              </p>
            </div>
          </div>

          {/* Monthly Chart */}
          <div className="history-chart">
            <h3>Monthly Revenue</h3>
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

          {/* Top Items */}
          <div className="top-items">
            <h3>Top Selling Items</h3>
            <div className="items-grid">
              {itemStats.map((item) => (
                <div key={item.name} className="item-stat-card">
                  <p className="item-name">{item.name}</p>
                  <p className="item-stat">
                    <span className="stat-label">Sold:</span>
                    <span className="stat-value">{item.quantity}</span>
                  </p>
                  <p className="item-stat">
                    <span className="stat-label">Revenue:</span>
                    <span className="stat-value">${item.revenue.toFixed(2)}</span>
                  </p>
                </div>
              ))}
            </div>
          </div>

          {/* Sales List */}
          <div className="sales-list">
            <h3>Recent Sales</h3>
            {sales.map((sale, index) => (
              <div key={index} className="sale-card">
                <div className="sale-card-header">
                  <div className="sale-info">
                    <p className="sale-item">{sale.itemName}</p>
                    <p className="sale-date">{formatDate(sale.createdAt)}</p>
                  </div>
                  <div className="sale-amount">
                    <p className="sale-quantity">x{sale.quantity}</p>
                    <p className="sale-total">${sale.totalPrice.toFixed(2)}</p>
                  </div>
                </div>
                <div className="sale-card-footer">
                  <span className="unit-price">
                    Unit Price: ${sale.unitPrice.toFixed(2)}
                  </span>
                </div>
              </div>
            ))}
          </div>
        </>
      )}
    </div>
  );
}
