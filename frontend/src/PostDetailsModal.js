import { useEffect, useState } from "react";
import { useI18n } from "./i18n/I18nContext";

export default function PostDetailsModal({ postId, onClose }) {
  const { t, formatDate, translateError } = useI18n();
  const [post, setPost] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (!postId) return;
    setLoading(true);
    setError(null);
    fetch(`/api/posts/${postId}`)
      .then(async (response) => {
        if (!response.ok) {
          throw new Error(t("errors.failedToFetchPost"));
        }
        return response.json();
      })
      .then((data) => {
        setPost(data);
        setLoading(false);
      })
      .catch((err) => {
        console.error("Error loading post:", err);
        setError(translateError(err.message, "errors.failedToFetchPost"));
        setLoading(false);
      });
  }, [postId, t, translateError]);

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div
        className="modal-content plant-details-modal"
        data-testid="post-details-modal"
        onClick={(e) => e.stopPropagation()}
      >
        <button
          className="modal-close-button"
          onClick={onClose}
          aria-label={t("forum.postDetails.closeAria")}
        >
          X
        </button>

        {loading ? (
          <p className="auth-error">{t("forum.postDetails.loading")}</p>
        ) : error ? (
          <p className="auth-error">{error}</p>
        ) : post ? (
          <article className="post-details">
            <header>
              <span className="auth-kicker">
                {post.categoryName || t("common.uncategorized")}
              </span>
              <h2 data-testid="post-details-title">{post.title}</h2>
              <p className="post-meta">
                {t("forum.postDetails.byLine", {
                  author: post.authorUsername || t("common.userFallback"),
                })}
                {post.createdAt && (
                  <>
                    {" · "}
                    {formatDate(post.createdAt, {
                      year: "numeric",
                      month: "short",
                      day: "numeric",
                    })}
                  </>
                )}
              </p>
            </header>

            <div className="post-body" data-testid="post-details-content">
              {post.content}
            </div>

            {/* Comments section — implemented by another contributor (items + posts). */}
            <section
              className="post-comments"
              data-testid="comments-section"
              data-post-id={post.id}
            >
              <h3>{t("forum.postDetails.commentsTitle")}</h3>
              <p className="auth-error">{t("forum.postDetails.commentsPlaceholder")}</p>
            </section>
          </article>
        ) : null}
      </div>
    </div>
  );
}
