import { useCallback, useEffect, useState } from "react";
import CreateForumPost from "./CreateForumPost";
import PostDetailsModal from "./PostDetailsModal";
import { useI18n } from "./i18n/I18nContext";

const SNIPPET_MAX = 180;

function snippet(content) {
  if (!content) return "";
  const trimmed = content.trim();
  if (trimmed.length <= SNIPPET_MAX) return trimmed;
  return trimmed.slice(0, SNIPPET_MAX).trim() + "…";
}

export default function Forum({ userId }) {
  const { t, formatDate, translateError } = useI18n();
  const [posts, setPosts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showCreate, setShowCreate] = useState(false);
  const [selectedPostId, setSelectedPostId] = useState(null);

  const loadPosts = useCallback(() => {
    setLoading(true);
    setError(null);
    fetch("/api/posts")
      .then(async (response) => {
        if (!response.ok) {
          throw new Error(t("errors.failedToFetchPosts"));
        }
        return response.json();
      })
      .then((data) => {
        setPosts(data || []);
        setLoading(false);
      })
      .catch((err) => {
        console.error("Error loading posts:", err);
        setError(translateError(err.message, "errors.failedToFetchPosts"));
        setLoading(false);
      });
  }, [t, translateError]);

  useEffect(() => {
    loadPosts();
  }, [loadPosts]);

  const handlePostCreated = () => {
    loadPosts();
  };

  return (
    <div className="forum-container" data-testid="forum">
      <div className="forum-header">
        <h2>{t("forum.title")}</h2>
        <button
          type="button"
          className="primary-button"
          data-testid="forum-create-post"
          onClick={() => setShowCreate(true)}
        >
          {t("forum.createPost.submit")}
        </button>
      </div>

      {loading ? (
        <p className="auth-error">{t("forum.loading")}</p>
      ) : error ? (
        <p className="auth-error">{error}</p>
      ) : posts.length === 0 ? (
        <p className="auth-error" data-testid="forum-empty">{t("forum.empty")}</p>
      ) : (
        <div className="forum-feed" data-testid="forum-feed">
          {posts.map((post) => (
            <article
              key={post.id}
              className="auth-card forum-post-card"
              data-testid="forum-post-card"
              onClick={() => setSelectedPostId(post.id)}
              role="button"
              tabIndex={0}
              onKeyDown={(e) => {
                if (e.key === "Enter" || e.key === " ") {
                  e.preventDefault();
                  setSelectedPostId(post.id);
                }
              }}
            >
              <div className="forum-post-meta">
                <span className="auth-kicker">
                  {post.categoryName || t("common.uncategorized")}
                </span>
                <span className="forum-post-author">
                  {post.authorUsername || t("common.userFallback")}
                </span>
                {post.createdAt && (
                  <span className="forum-post-date">
                    {formatDate(post.createdAt, {
                      year: "numeric",
                      month: "short",
                      day: "numeric",
                    })}
                  </span>
                )}
              </div>
              <h3 data-testid="forum-post-card-title">{post.title}</h3>
              <p className="forum-post-snippet">{snippet(post.content)}</p>
            </article>
          ))}
        </div>
      )}

      {showCreate && (
        <CreateForumPost
          userId={userId}
          onClose={() => setShowCreate(false)}
          onPostCreated={handlePostCreated}
        />
      )}

      {selectedPostId && (
        <PostDetailsModal
          postId={selectedPostId}
          userId={userId}
          onClose={() => setSelectedPostId(null)}
        />
      )}
    </div>
  );
}
