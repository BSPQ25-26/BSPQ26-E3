import { useEffect, useState } from "react";
import { useI18n } from "./i18n/I18nContext";

export default function CreateForumPost({ userId, onClose, onPostCreated }) {
  const { t, translateError } = useI18n();
  const [title, setTitle] = useState("");
  const [content, setContent] = useState("");
  const [categoryId, setCategoryId] = useState("");
  const [categories, setCategories] = useState([]);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetch("/api/categories")
      .then((r) => (r.ok ? r.json() : []))
      .then((data) => {
        setCategories(data || []);
        if (data && data.length > 0) {
          setCategoryId(String(data[0].id));
        }
      })
      .catch(() => setCategories([]));
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);

    if (!title.trim()) {
      setError(t("errors.titleRequired"));
      return;
    }

    setSubmitting(true);
    try {
      const response = await fetch("/api/posts", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          authorId: userId,
          title: title.trim(),
          content: content.trim(),
          categoryId: categoryId ? Number(categoryId) : null,
        }),
      });

      if (!response.ok) {
        const text = await response.text().catch(() => "");
        throw new Error(text || t("errors.failedToCreatePost"));
      }

      const created = await response.json();
      if (onPostCreated) {
        onPostCreated(created);
      }
      onClose();
    } catch (err) {
      console.error("Error creating post:", err);
      setError(translateError(err.message, "errors.failedToCreatePost"));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div
        className="modal-content create-forum-post-modal"
        data-testid="create-forum-post-modal"
        onClick={(e) => e.stopPropagation()}
      >
        <button
          className="modal-close-button"
          onClick={onClose}
          aria-label={t("forum.createPost.closeAria")}
        >
          X
        </button>

        <div className="modal-header">
          <h2>{t("forum.createPost.title")}</h2>
          <p className="form-subtitle">{t("forum.createPost.subtitle")}</p>
        </div>

        <form className="create-forum-post-form" onSubmit={handleSubmit}>
          <div className="form-section">
            <div className="form-group">
              <label htmlFor="forum-post-title">{t("forum.createPost.titleLabel")}</label>
              <input
                id="forum-post-title"
                data-testid="forum-post-title"
                className="auth-input"
                type="text"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                placeholder={t("forum.createPost.titlePlaceholder")}
                disabled={submitting}
              />
            </div>

            <div className="form-group">
              <label htmlFor="forum-post-content">{t("forum.createPost.contentLabel")}</label>
              <textarea
                id="forum-post-content"
                data-testid="forum-post-content"
                className="auth-input form-textarea"
                value={content}
                onChange={(e) => setContent(e.target.value)}
                placeholder={t("forum.createPost.contentPlaceholder")}
                rows="6"
                disabled={submitting}
              />
            </div>

            <div className="form-group">
              <label htmlFor="forum-post-category">{t("forum.createPost.categoryLabel")}</label>
              <select
                id="forum-post-category"
                data-testid="forum-post-category"
                className="auth-input"
                value={categoryId}
                onChange={(e) => setCategoryId(e.target.value)}
                disabled={submitting || categories.length === 0}
              >
                {categories.map((c) => (
                  <option key={c.id} value={c.id}>{c.name}</option>
                ))}
              </select>
            </div>
          </div>

          {error && <div className="auth-error" data-testid="forum-post-error">{error}</div>}
        </form>

        <div className="form-actions">
          <button
            type="button"
            className="secondary-button"
            onClick={onClose}
            disabled={submitting}
          >
            {t("common.actions.cancel")}
          </button>
          <button
            type="submit"
            className="primary-button"
            data-testid="forum-post-submit"
            disabled={submitting}
            onClick={handleSubmit}
          >
            {submitting ? t("common.actions.creating") : t("forum.createPost.submit")}
          </button>
        </div>
      </div>
    </div>
  );
}
