const API_URL = "/api/comments";

export const fetchComments = async (targetId, type) => {
    const response = await fetch(`${API_URL}/${type}/${targetId}`);
    if (!response.ok) throw new Error("Error al cargar comentarios");
    return response.json();
};

export const createComment = async (commentData) => {
    const response = await fetch(API_URL, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(commentData),
    });
    if (!response.ok) throw new Error("Error al publicar el comentario");
    return response.json();
};

export const updateComment = async (commentId, requesterId, content) => {
    const response = await fetch(`${API_URL}/${commentId}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ requesterId, content }),
    });
    if (!response.ok) throw new Error("Error al editar el comentario");
    return response.json();
};

export const deleteComment = async (commentId, requesterId) => {
    const response = await fetch(`${API_URL}/${commentId}?requesterId=${requesterId}`, {
        method: "DELETE",
    });
    if (!response.ok) throw new Error("Error al eliminar el comentario");
};