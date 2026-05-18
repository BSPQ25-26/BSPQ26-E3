import { useCallback, useEffect, useState } from "react";
import { useI18n } from "./i18n/I18nContext";

export default function PostDetailsModal({ postId, userId, onClose }) {
  const { t, formatDate, translateError } = useI18n();
  const [post, setPost] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [comments, setComments] = useState([]);
  const [commentsLoading, setCommentsLoading] = useState(false);
  const [commentsError, setCommentsError] = useState("");
  const [commentContent, setCommentContent] = useState("");
  const [submittingComment, setSubmittingComment] = useState(false);
  const [commentMessage, setCommentMessage] = useState(null);
  const [deletingCommentId, setDeletingCommentId] = useState(null);

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

  const loadComments = useCallback(() => {
    if (!postId) return;
    setCommentsLoading(true);
    setCommentsError("");

    fetch(`/api/posts/${postId}/comments`)
      .then(async (response) => {
        if (!response.ok) {
          throw new Error(t("errors.failedToFetchComments"));
        }
        return response.json();
      })
      .then((data) => {
        setComments(Array.isArray(data) ? data : []);
        setCommentsLoading(false);
      })
      .catch((err) => {
        console.error("Error loading comments:", err);
        setCommentsError(translateError(err.message, "errors.failedToFetchComments"));
        setCommentsLoading(false);
      });
  }, [postId, t, translateError]);

  useEffect(() => {
    loadComments();
  }, [loadComments]);

  const handleCommentSubmit = async (event) => {
    event.preventDefault();

    if (!userId) {
      setCommentMessage({ type: "error", text: t("plantDetails.userIdNotFound") });
      return;
    }

    setSubmittingComment(true);
    setCommentMessage(null);

    try {
      const response = await fetch(`/api/posts/${postId}/comments`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ authorId: userId, content: commentContent }),
      });

      if (!response.ok) {
        let errorMessage = t("errors.failedToCreateComment");
        try {
          const errorData = await response.json();
          if (errorData.message) errorMessage = errorData.message;
        } catch (_) {}
        throw new Error(errorMessage);
      }

      setCommentContent("");
      setCommentMessage({ type: "success", text: t("forum.postDetails.commentSuccess") });
      loadComments();
    } catch (err) {
      console.error("Error creating comment:", err);
      setCommentMessage({ type: "error", text: translateError(err.message, "errors.failedToCreateComment") });
    } finally {
      setSubmittingComment(false);
    }
  };

  const handleDeleteComment = async (commentId) => {
    if (!userId) return;

    setDeletingCommentId(commentId);
    try {
      const response = await fetch(
        `/api/posts/${postId}/comments/${commentId}?requesterId=${userId}`,
        { method: "DELETE" }
      );

      if (!response.ok) {
        let errorMessage = t("errors.failedToDeleteComment");
        try {
          const errorData = await response.json();
          if (errorData.message) errorMessage = errorData.message;
        } catch (_) {}
        throw new Error(errorMessage);
      }

      setComments((current) => current.filter((c) => c.id !== commentId));
    } catch (err) {
      console.error("Error deleting comment:", err);
      setCommentMessage({ type: "error", text: translateError(err.message, "errors.failedToDeleteComment") });
    } finally {
      setDeletingCommentId(null);
    }
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div
        className="modal-content post-details-modal"
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

            <section
              className="post-comments"
              data-testid="comments-section"
              data-post-id={post.id}
            >
              <h3>{t("forum.postDetails.commentsTitle")}</h3>

              <form className="review-form" onSubmit={handleCommentSubmit}>
                <label className="review-form-field" htmlFor="comment-content">
                  <span>{t("forum.postDetails.commentFormTitle")}</span>
                  <textarea
                    id="comment-content"
                    data-testid="comment-input"
                    value={commentContent}
                    onChange={(e) => setCommentContent(e.target.value)}
                    placeholder={t("forum.postDetails.commentPlaceholder")}
                    rows="3"
                    disabled={submittingComment}
                  />
                </label>

                {commentMessage && (
                  <div className={`auth-${commentMessage.type === "success" ? "notice" : "error"}`}>
                    {commentMessage.text}
                  </div>
                )}

                <button
                  className="primary-button review-submit-button"
                  data-testid="comment-submit"
                  type="submit"
                  disabled={submittingComment || !commentContent.trim()}
                >
                  {submittingComment
                    ? t("forum.postDetails.commentSubmitting")
                    : t("forum.postDetails.commentSubmit")}
                </button>
              </form>

              {commentsLoading ? (
                <p className="plant-reviews-state">{t("forum.postDetails.commentsLoading")}</p>
              ) : commentsError ? (
                <p className="auth-error">{commentsError}</p>
              ) : comments.length === 0 ? (
                <p className="forum-comments-placeholder">{t("forum.postDetails.commentsEmpty")}</p>
              ) : (
                <div className="reviews-list">
                  {comments.map((comment) => (
                    <article className="review-card" key={comment.id} data-testid="comment-card">
                      <div className="review-card-header">
                        <div>
                          <h4>{comment.authorUsername || t("common.userFallback")}</h4>
                          <p className="review-card-meta">
                            {comment.createdAt
                              ? formatDate(comment.createdAt, {
                                  year: "numeric",
                                  month: "short",
                                  day: "numeric",
                                })
                              : ""}
                          </p>
                        </div>
                        {userId && comment.authorId === userId && (
                          <button
                            className="delete-button"
                            data-testid="comment-delete-button"
                            onClick={() => handleDeleteComment(comment.id)}
                            disabled={deletingCommentId === comment.id}
                            aria-label={t("forum.postDetails.commentDelete")}
                          >
                            {deletingCommentId === comment.id
                              ? t("plantDetails.reviewDeleting")
                              : t("forum.postDetails.commentDelete")}
                          </button>
                        )}
                      </div>
                      <p className="review-card-comment">{comment.content}</p>
                    </article>
                  ))}
                </div>
              )}
            </section>
          </article>
        ) : null}
      </div>
    </div>
  );
}
