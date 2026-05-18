import React from "react";
import { useI18n } from "./i18n/I18nContext";

export default function PlantDetailsModal({ plantId, userId, onClose, onItemAdded }) {
  const { t, formatCurrency, formatDate, translateCategory, translateItemStatus, translateError } = useI18n();
  const [plantDetails, setPlantDetails] = React.useState(null);
  const [loading, setLoading] = React.useState(false);
  const [reviewsLoading, setReviewsLoading] = React.useState(false);
  const [reviews, setReviews] = React.useState([]);
  const [reviewsError, setReviewsError] = React.useState("");
  const [quantity, setQuantity] = React.useState(1);
  const [addingToCart, setAddingToCart] = React.useState(false);
  const [cartMessage, setCartMessage] = React.useState(null);
  const [reviewRating, setReviewRating] = React.useState(5);
  const [reviewComment, setReviewComment] = React.useState("");
  const [submittingReview, setSubmittingReview] = React.useState(false);
  const [reviewMessage, setReviewMessage] = React.useState(null);

  React.useEffect(() => {
    setLoading(true);

    fetch(`/api/items/${plantId}`)
      .then(async (response) => {
        if (!response.ok) {
          throw new Error(t("errors.failedToFetchPlantDetails"));
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
  }, [plantId, t]);

  React.useEffect(() => {
    let isMounted = true;

    setReviewsLoading(true);
    setReviewsError("");
    setReviewMessage(null);

    fetch(`/api/items/${plantId}/reviews`)
      .then(async (response) => {
        if (!response.ok) {
          throw new Error(t("errors.failedToFetchReviews"));
        }
        return response.json();
      })
      .then((data) => {
        if (!isMounted) {
          return;
        }
        setReviews(Array.isArray(data) ? data : []);
        setReviewsLoading(false);
      })
      .catch((err) => {
        if (!isMounted) {
          return;
        }
        console.error("Error loading reviews:", err);
        setReviewsError(translateError(err.message, "errors.failedToFetchReviews"));
        setReviewsLoading(false);
      });

    return () => {
      isMounted = false;
    };
  }, [plantId, t, translateError]);

  const handleAddToCart = async () => {
    if (!userId) {
      setCartMessage({ type: "error", text: t("plantDetails.userIdNotFound") });
      return;
    }

    setAddingToCart(true);
    setCartMessage(null);

    try {
      const response = await fetch(`/api/carts/${userId}/items`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          itemId: plantId,
          quantity,
        }),
      });

      if (!response.ok) {
        let errorMessage = t("errors.errorAddingToCart");
        try {
          const errorData = await response.json();
          if (errorData.message) {
            errorMessage = errorData.message;
          }
        } catch (error) {
          if (response.statusText) {
            errorMessage = response.statusText;
          }
        }
        throw new Error(errorMessage);
      }

      setCartMessage({ type: "success", text: t("plantDetails.addedToCart") });
      if (onItemAdded) {
        onItemAdded(quantity);
      }
      setTimeout(() => {
        onClose();
      }, 1500);
    } catch (err) {
      console.error("Error adding to cart:", err);
      setCartMessage({ type: "error", text: translateError(err.message, "errors.errorAddingToCart") });
    } finally {
      setAddingToCart(false);
    }
  };

  const handleReviewSubmit = async (event) => {
    event.preventDefault();

    if (!userId) {
      setReviewMessage({ type: "error", text: t("plantDetails.userIdNotFound") });
      return;
    }

    setSubmittingReview(true);
    setReviewMessage(null);

    try {
      const response = await fetch(`/api/items/${plantId}/reviews`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          authorId: userId,
          rating: Number(reviewRating),
          comment: reviewComment,
        }),
      });

      if (!response.ok) {
        let errorMessage = t("errors.failedToCreateReview");
        try {
          const errorData = await response.json();
          if (errorData.message) {
            errorMessage = errorData.message;
          }
        } catch (error) {
          if (response.statusText) {
            errorMessage = response.statusText;
          }
        }
        throw new Error(errorMessage);
      }

      const createdReview = await response.json();
      setReviews((currentReviews) => [createdReview, ...currentReviews]);
      setReviewRating(5);
      setReviewComment("");
      setReviewMessage({ type: "success", text: t("plantDetails.reviewSuccess") });
    } catch (err) {
      console.error("Error creating review:", err);
      setReviewMessage({ type: "error", text: translateError(err.message, "errors.failedToCreateReview") });
    } finally {
      setSubmittingReview(false);
    }
  };

  const renderReviewRating = (rating) => `${Number(rating ?? 0)}/5`;

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content plant-details-modal" data-testid="plant-details-modal" onClick={(e) => e.stopPropagation()}>
        {loading ? (
          <p className="auth-error">{t("plantDetails.loading")}</p>
        ) : plantDetails ? (
          <>
            <button
              className="modal-close-button"
              onClick={onClose}
              aria-label={t("plantDetails.closeAria")}
            >
              X
            </button>
            <div className="plant-details-layout">
              <div className="plant-details-grid">
                <div className="plant-details-image">
                  <img
                    src={plantDetails.image_url || "https://via.placeholder.com/400"}
                    alt={plantDetails.title}
                  />
                </div>
                <div className="plant-details-info">
                  <span className="auth-kicker">
                    {plantDetails.categoryName ? translateCategory(plantDetails.categoryName) : t("common.uncategorized")}
                  </span>
                  <h2>{plantDetails.title}</h2>
                  <p className="plant-details-price">{formatCurrency(plantDetails.amount)}</p>

                  <div className="plant-details-specs">
                    <div className="spec-item">
                      <dt>{t("common.labels.description")}</dt>
                      <dd>{plantDetails.description || t("plantDetails.noDescription")}</dd>
                    </div>
                    <div className="spec-item">
                      <dt>{t("plantDetails.stockAvailable")}</dt>
                      <dd>{t("plantDetails.units", { count: plantDetails.quantity })}</dd>
                    </div>
                    <div className="spec-item">
                      <dt>{t("common.labels.status")}</dt>
                      <dd>{translateItemStatus(plantDetails.status)}</dd>
                    </div>
                  </div>

                  {cartMessage && (
                    <div className={`auth-${cartMessage.type === "success" ? "notice" : "error"}`}>
                      {cartMessage.text}
                    </div>
                  )}

                  <div className="quantity-selector">
                    <label htmlFor="quantity">{t("plantDetails.quantityLabel")}</label>
                    <input
                      id="quantity"
                      data-testid="plant-quantity"
                      type="number"
                      min="1"
                      max={plantDetails.quantity}
                      value={quantity}
                      onChange={(e) => setQuantity(Math.max(1, parseInt(e.target.value, 10) || 1))}
                      disabled={addingToCart}
                    />
                  </div>

                  <button
                    className="primary-button"
                    data-testid="plant-add-to-cart"
                    onClick={handleAddToCart}
                    disabled={addingToCart}
                  >
                    {addingToCart ? t("common.actions.adding") : t("common.actions.addToCart")}
                  </button>
                </div>
              </div>

              <section className="plant-reviews-section">
                <div className="plant-reviews-header">
                  <div>
                    <span className="auth-kicker">{t("plantDetails.reviewsTitle")}</span>
                    <h3>{t("plantDetails.reviewFormTitle")}</h3>
                  </div>
                </div>

                <form className="review-form" onSubmit={handleReviewSubmit}>
                  <div className="review-form-row">
                    <label className="review-form-field" htmlFor="review-rating">
                      <span>{t("common.labels.rating")}</span>
                      <select
                        id="review-rating"
                        value={reviewRating}
                        onChange={(e) => setReviewRating(Number(e.target.value))}
                        disabled={submittingReview}
                      >
                        <option value={5}>5</option>
                        <option value={4}>4</option>
                        <option value={3}>3</option>
                        <option value={2}>2</option>
                        <option value={1}>1</option>
                      </select>
                    </label>
                    <label className="review-form-field" htmlFor="review-comment">
                      <span>{t("common.labels.comment")}</span>
                      <textarea
                        id="review-comment"
                        value={reviewComment}
                        onChange={(e) => setReviewComment(e.target.value)}
                        placeholder={t("plantDetails.reviewCommentPlaceholder")}
                        rows="3"
                        disabled={submittingReview}
                      />
                    </label>
                  </div>

                  {reviewMessage && (
                    <div className={`auth-${reviewMessage.type === "success" ? "notice" : "error"}`}>
                      {reviewMessage.text}
                    </div>
                  )}

                  <button className="primary-button review-submit-button" type="submit" disabled={submittingReview}>
                    {submittingReview ? t("plantDetails.reviewSubmitting") : t("plantDetails.reviewSubmit")}
                  </button>
                </form>

                {reviewsLoading ? (
                  <p className="plant-reviews-state">{t("plantDetails.reviewsLoading")}</p>
                ) : reviewsError ? (
                  <p className="auth-error">{reviewsError}</p>
                ) : reviews.length === 0 ? (
                  <p className="plant-reviews-state">{t("plantDetails.reviewsEmpty")}</p>
                ) : (
                  <div className="reviews-list">
                    {reviews.map((review) => (
                      <article className="review-card" key={review.id}>
                        <div className="review-card-header">
                          <h4>{review.authorUsername || t("common.userFallback")}</h4>
                          <p className="review-card-meta">
                            {renderReviewRating(review.rating)}
                            {review.createdAt ? ` - ${formatDate(review.createdAt, {
                              dateStyle: "medium",
                              timeStyle: "short",
                            })}` : ""}
                          </p>
                        </div>
                        <p className="review-card-comment">
                          {review.comment || t("plantDetails.reviewNoComment")}
                        </p>
                      </article>
                    ))}
                  </div>
                )}
              </section>
            </div>
          </>
        ) : (
          <p className="auth-error">{t("plantDetails.error")}</p>
        )}
      </div>
    </div>
  );
}
