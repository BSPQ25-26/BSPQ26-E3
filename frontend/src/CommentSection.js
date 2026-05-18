import React, { useState, useEffect } from 'react';
import { fetchComments, createComment, updateComment, deleteComment } from './api';
import { useI18n } from './i18n/I18nContext';

const CommentSection = ({ targetId, targetType, currentUserId }) => {
    const { t } = useI18n();
    const [comments, setComments] = useState([]);
    const [newCommentText, setNewCommentText] = useState("");
    const [loading, setLoading] = useState(true);
    const [isAdding, setIsAdding] = useState(false);
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState(null);

    // Estado de edición: { [commentId]: { active: bool, text: string, submitting: bool } }
    const [editState, setEditState] = useState({});

    useEffect(() => {
        const loadComments = async () => {
            try {
                const data = await fetchComments(targetId, targetType);
                setComments(data);
            } catch (error) {
                console.error("Error al cargar comentarios:", error);
            } finally {
                setLoading(false);
            }
        };
        loadComments();
    }, [targetId, targetType]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        e.stopPropagation();

        if (!newCommentText.trim()) return;

        if (!currentUserId) {
            setError(t('comments.errorNoSession'));
            return;
        }

        setSubmitting(true);
        setError(null);

        const commentPayload = {
            content: newCommentText,
            authorId: currentUserId,
            postId: targetType === 'post' ? targetId : null,
            itemId: targetType === 'item' ? targetId : null,
        };

        try {
            const savedComment = await createComment(commentPayload);
            setComments([...comments, savedComment]);
            setNewCommentText("");
            setIsAdding(false);
        } catch (error) {
            console.error("Error al publicar:", error);
            setError(t('comments.errorPublish'));
        } finally {
            setSubmitting(false);
        }
    };

    const startEdit = (comment) => {
        setEditState(prev => ({
            ...prev,
            [comment.id]: { active: true, text: comment.content, submitting: false }
        }));
    };

    const cancelEdit = (commentId) => {
        setEditState(prev => ({ ...prev, [commentId]: { active: false, text: "", submitting: false } }));
    };

    const handleEdit = async (commentId) => {
        const state = editState[commentId];
        if (!state || !state.text.trim()) return;

        setEditState(prev => ({ ...prev, [commentId]: { ...prev[commentId], submitting: true } }));

        try {
            const updated = await updateComment(commentId, currentUserId, state.text);
            setComments(comments.map(c => c.id === commentId ? updated : c));
            setEditState(prev => ({ ...prev, [commentId]: { active: false, text: "", submitting: false } }));
        } catch (error) {
            console.error("Error al editar:", error);
            setEditState(prev => ({ ...prev, [commentId]: { ...prev[commentId], submitting: false } }));
        }
    };

    const handleDelete = async (commentId) => {
        try {
            await deleteComment(commentId, currentUserId);
            setComments(comments.filter(c => c.id !== commentId));
        } catch (error) {
            console.error("Error al eliminar:", error);
        }
    };

    if (loading) return <p className="comment-loading">{t('comments.loading')}</p>;

    return (
        <div className="comment-section" onClick={(e) => e.stopPropagation()}>
            <div className="comment-header">
                <h3>{t('comments.title', { count: comments.length })}</h3>
                {!isAdding && (
                    <button
                        className="add-comment-btn"
                        onClick={(e) => {
                            e.preventDefault();
                            e.stopPropagation();
                            setIsAdding(true);
                        }}
                    >
                        {t('comments.addButton')}
                    </button>
                )}
            </div>

            {isAdding && (
                <div className="comment-form-container">
                    {error && <div className="comment-error-alert">{error}</div>}
                    <textarea
                        className="comment-textarea"
                        value={newCommentText}
                        onChange={(e) => setNewCommentText(e.target.value)}
                        placeholder={t('comments.placeholder')}
                        required
                        disabled={submitting}
                        autoFocus
                    />
                    <div className="comment-actions">
                        <button
                            type="button"
                            className="cancel-btn"
                            onClick={(e) => {
                                e.preventDefault();
                                e.stopPropagation();
                                setIsAdding(false);
                                setError(null);
                                setNewCommentText("");
                            }}
                            disabled={submitting}
                        >
                            {t('comments.cancel')}
                        </button>
                        <button
                            type="button"
                            className="submit-btn"
                            onClick={handleSubmit}
                            disabled={submitting || !newCommentText.trim()}
                        >
                            {submitting ? t('comments.publishing') : t('comments.publish')}
                        </button>
                    </div>
                </div>
            )}

            <div className="comments-list">
                {comments.length === 0 ? (
                    <p className="no-comments-msg">{t('comments.noComments')}</p>
                ) : (
                    comments.map((comment) => {
                        const isOwner = String(comment.authorId) === String(currentUserId);
                        const editing = editState[comment.id];

                        return (
                            <div key={comment.id} className="comment-item">
                                <div className="comment-meta">
                                    <span className="comment-author">{comment.authorName}</span>
                                    <span className="comment-date">
                                        • {new Date(comment.createdAt).toLocaleDateString()}
                                    </span>
                                    {comment.edited && (
                                        <span className="comment-edited-badge">{t('comments.edited')}</span>
                                    )}
                                    {isOwner && !editing?.active && (
                                        <div className="comment-owner-actions">
                                            <button
                                                className="comment-action-btn edit"
                                                onClick={() => startEdit(comment)}
                                                title={t('comments.editTitle')}
                                            >
                                                ✏️
                                            </button>
                                            <button
                                                className="comment-action-btn delete"
                                                onClick={() => handleDelete(comment.id)}
                                                title={t('comments.deleteTitle')}
                                            >
                                                🗑️
                                            </button>
                                        </div>
                                    )}
                                </div>

                                {editing?.active ? (
                                    <div className="comment-edit-container">
                                        <textarea
                                            className="comment-textarea"
                                            value={editing.text}
                                            onChange={(e) => setEditState(prev => ({
                                                ...prev,
                                                [comment.id]: { ...prev[comment.id], text: e.target.value }
                                            }))}
                                            disabled={editing.submitting}
                                            autoFocus
                                        />
                                        <div className="comment-actions">
                                            <button
                                                type="button"
                                                className="cancel-btn"
                                                onClick={() => cancelEdit(comment.id)}
                                                disabled={editing.submitting}
                                            >
                                                {t('comments.cancel')}
                                            </button>
                                            <button
                                                type="button"
                                                className="submit-btn"
                                                onClick={() => handleEdit(comment.id)}
                                                disabled={editing.submitting || !editing.text.trim()}
                                            >
                                                {editing.submitting ? t('comments.saving') : t('comments.save')}
                                            </button>
                                        </div>
                                    </div>
                                ) : (
                                    <p className="comment-body">{comment.content}</p>
                                )}
                            </div>
                        );
                    })
                )}
            </div>
        </div>
    );
};

export default CommentSection;