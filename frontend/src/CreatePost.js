import React, { useState } from "react";

const BUCKET_NAME = "plants";

export default function CreatePost({ userId, onClose, onPostCreated }) {
  const [formData, setFormData] = useState({
    title: "",
    description: "",
    amount: "",
    quantity: "",
    category: "Indoor",
    status: true,
  });

  const [imageFile, setImageFile] = useState(null);
  const [imagePreview, setImagePreview] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(false);
  const [uploadingImage, setUploadingImage] = useState(false);

  const handleInputChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: type === "checkbox" ? checked : value,
    }));
  };

  const handleImageChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      if (!file.type.startsWith("image/")) {
        setError("Please select a valid image file");
        return;
      }

      setImageFile(file);
      const reader = new FileReader();
      reader.onloadend = () => {
        setImagePreview(reader.result);
      };
      reader.readAsDataURL(file);
      setError(null);
    }
  };

  const uploadImageToSupabase = async (file) => {
    const SUPABASE_URL = process.env.REACT_APP_SUPABASE_URL;
    const SUPABASE_ANON_KEY = process.env.REACT_APP_SUPABASE_ANON_KEY;

    if (!SUPABASE_URL || !SUPABASE_ANON_KEY) {
      throw new Error("Supabase is not configured. Missing REACT_APP_SUPABASE_* variables.");
    }

    const timestamp = Date.now();
    const fileName = `${timestamp}-${file.name}`;
    const filePath = `${fileName}`;

    try {
      const response = await fetch(
        `${SUPABASE_URL}/storage/v1/object/${BUCKET_NAME}/${filePath}`,
        {
          method: "POST",
          headers: {
            apikey: SUPABASE_ANON_KEY,
            Authorization: `Bearer ${SUPABASE_ANON_KEY}`,
            "Content-Type": file.type,
            "x-upsert": "false",
          },
          body: file,
        }
      );

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(
          `Supabase upload failed (${response.status}): ${errorText || response.statusText}`
        );
      }

      const publicUrl = `${SUPABASE_URL}/storage/v1/object/public/${BUCKET_NAME}/${filePath}`;
      return publicUrl;
    } catch (err) {
      console.error("Error uploading image:", err);
      throw err;
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setSuccess(false);

    if (!formData.title.trim()) {
      setError("Title is required");
      return;
    }

    if (!formData.description.trim()) {
      setError("Description is required");
      return;
    }

    if (!formData.amount || parseFloat(formData.amount) <= 0) {
      setError("Price must be greater than 0");
      return;
    }

    if (!formData.quantity || parseInt(formData.quantity) <= 0) {
      setError("Quantity must be greater than 0");
      return;
    }

    setLoading(true);

    try {
      let imageUrl = "";

      if (imageFile) {
        setUploadingImage(true);
        try {
          imageUrl = await uploadImageToSupabase(imageFile);
        } finally {
          setUploadingImage(false);
        }
      }

      const itemData = {
        title: formData.title,
        description: formData.description,
        amount: parseFloat(formData.amount),
        quantity: parseInt(formData.quantity),
        category: {
          name: formData.category,
        },
        status: formData.status,
        imagen: imageUrl || "", // Spring busca setImagen()
      };

      const response = await fetch("/api/items", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "X-User-Id": userId,
        },
        body: JSON.stringify(itemData),
      });

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(errorText || "Failed to create item");
      }

      setSuccess(true);
      if (onPostCreated) {
        onPostCreated();
      }

      setTimeout(() => {
        onClose();
      }, 1500);
    } catch (err) {
      console.error("Error creating item:", err);
      setError(err.message || "Error creating item");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div
        className="modal-content create-post-modal"
        onClick={(e) => e.stopPropagation()}
      >
        <button
          className="modal-close-button"
          onClick={onClose}
          aria-label="Close form"
        >
          ✕
        </button>

        <div className="modal-header">
          <h2>Create New Post</h2>
          <p className="form-subtitle">Share your plant with the community</p>
        </div>

        <form onSubmit={handleSubmit} className="create-post-form">
          {/* Image Upload Section */}
          <div className="form-section">
            <h3>Product Image (Optional)</h3>
            <div className="image-upload-area">
              {imagePreview ? (
                <div className="image-preview-container">
                  <img src={imagePreview} alt="Preview" />
                  <button
                    type="button"
                    className="change-image-button"
                    onClick={() => {
                      setImageFile(null);
                      setImagePreview(null);
                    }}
                  >
                    Change Image
                  </button>
                </div>
              ) : (
                <label className="image-upload-label">
                  <svg viewBox="0 0 24 24" aria-hidden="true">
                    <path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0 16H5V5h14v14zm-5.04-6.71l-2.75-3.54-4.04 5.25h12l-3.21-4.21z" />
                  </svg>
                  <p>Click to upload image (optional)</p>
                  <input
                    type="file"
                    accept="image/*"
                    onChange={handleImageChange}
                    disabled={loading}
                  />
                </label>
              )}
            </div>
          </div>

          {/* Product Details Section */}
          <div className="form-section">
            <h3>Product Details</h3>

            <div className="form-group">
              <label htmlFor="title">Title *</label>
              <input
                id="title"
                type="text"
                name="title"
                className="auth-input"
                value={formData.title}
                onChange={handleInputChange}
                placeholder="e.g., Monstera Deliciosa"
                disabled={loading}
              />
            </div>

            <div className="form-group">
              <label htmlFor="description">Description *</label>
              <textarea
                id="description"
                name="description"
                className="auth-input form-textarea"
                value={formData.description}
                onChange={handleInputChange}
                placeholder="Describe your plant (care instructions, size, etc.)"
                rows="3"
                disabled={loading}
              />
            </div>

            <div className="form-row">
              <div className="form-group">
                <label htmlFor="amount">Price ($) *</label>
                <input
                  id="amount"
                  type="number"
                  name="amount"
                  className="auth-input"
                  value={formData.amount}
                  onChange={handleInputChange}
                  placeholder="0.00"
                  step="0.01"
                  min="0"
                  disabled={loading}
                />
              </div>

              <div className="form-group">
                <label htmlFor="quantity">Quantity *</label>
                <input
                  id="quantity"
                  type="number"
                  name="quantity"
                  className="auth-input"
                  value={formData.quantity}
                  onChange={handleInputChange}
                  placeholder="1"
                  min="1"
                  disabled={loading}
                />
              </div>
            </div>

            <div className="form-row">
              <div className="form-group">
                <label htmlFor="category">Category *</label>
                <select
                  id="category"
                  name="category"
                  className="auth-input"
                  value={formData.category}
                  onChange={handleInputChange}
                  disabled={loading}
                >
                  <option value="Indoor">Indoor</option>
                  <option value="Outdoor">Outdoor</option>
                  <option value="Succulent">Succulent</option>
                  <option value="Flowering">Flowering</option>
                </select>
              </div>

              <div className="form-group checkbox-group">
                <label htmlFor="status">
                  <input
                    id="status"
                    type="checkbox"
                    name="status"
                    checked={formData.status}
                    onChange={handleInputChange}
                    disabled={loading}
                  />
                  Active Status
                </label>
              </div>
            </div>
          </div>

          {/* Messages */}
          {error && <div className="auth-error">{error}</div>}
          {success && (
            <div className="auth-notice">
              Post created successfully! Redirecting...
            </div>
          )}
        </form>

        {/* Action Buttons - Fijos al final */}
        <div className="form-actions">
          <button
            type="button"
            className="secondary-button"
            onClick={onClose}
            disabled={loading}
          >
            Cancel
          </button>
          <button
            type="submit"
            className="primary-button"
            disabled={loading || uploadingImage}
            onClick={handleSubmit}
          >
            {loading ? "Creating..." : uploadingImage ? "Uploading Image..." : "Create Post"}
          </button>
        </div>
      </div>
    </div>
  );
}