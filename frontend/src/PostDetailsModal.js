import { useEffect, useState } from "react";
import { useI18n } from "./i18n/I18nContext";
import CommentSection from './CommentSection';

export default function PostDetailsModal({ postId, userId, onClose }) {
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
      {/* Le hemos quitado el onClick a este div para que no interfiera 
        con los clics internos de la caja de comentarios.
      */}
      <div
        className="modal-content post-details-modal"
        data-testid="post-details-modal"
      >
        <button
          className="modal-close-button"
          onClick={onClose}
          aria-label={t("forum.postDetails.closeAria")}
        >
          X
        </button>

        {loading ? (
          <p className="post-details-loading">{t("forum.postDetails.loading")}</p>
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

            <hr style={{ margin: "2rem 0", borderTop: "1px solid #e5e7eb" }} />
            
            {/* Este div actúa como una barrera. Atrapa cualquier clic dentro 
               de los comentarios para que no llegue al "modal-overlay" 
               (que es el que cierra la ventana)
            */}
            <div 
              className="comments-wrapper" 
              onClick={(e) => e.stopPropagation()}
              style={{ paddingBottom: '20px' }}
            >
              <CommentSection 
                targetId={post.id} 
                targetType="post" 
                currentUserId={userId} 
              />
            </div>
          </article>
        ) : null}
      </div>
    </div>
  );
}
