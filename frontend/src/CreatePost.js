import React, { useState } from "react";
import { useI18n } from "./i18n/I18nContext";

const BUCKET_NAME = "plants";

export default function CreatePost({ userId, onClose, onPostCreated }) {
  const { t, translateCategory, translateError } = useI18n();
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
        setError(t("errors.imageFileInvalid"));
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
      throw new Error(t("errors.supabaseMissing"));
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
        throw new Error(`Supabase upload failed (${response.status}): ${errorText || response.statusText}`);
      }

      return `${SUPABASE_URL}/storage/v1/object/public/${BUCKET_NAME}/${filePath}`;
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
      setError(t("errors.titleRequired"));
      return;
    }

    if (!formData.description.trim()) {
      setError(t("errors.descriptionRequired"));
      return;
    }

    if (!formData.amount || parseFloat(formData.amount) <= 0) {
      setError(t("errors.pricePositive"));
      return;
    }

    if (!formData.quantity || parseInt(formData.quantity, 10) <= 0) {
      setError(t("errors.quantityPositive"));
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
        content: formData.description,
        amount: parseFloat(formData.amount),
        quantity: parseInt(formData.quantity, 10),
        category: {
          name: formData.category,
        },
        isPublic: formData.status,
        imagen: imageUrl || "",
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
        throw new Error(errorText || t("errors.failedToCreateItem"));
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
      setError(translateError(err.message, "errors.errorCreatingItem"));
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
          aria-label={t("createPost.closeAria")}
        >
          X
        </button>

        <div className="modal-header">
          <h2>{t("createPost.title")}</h2>
          <p className="form-subtitle">{t("createPost.subtitle")}</p>
        </div>

        <form onSubmit={handleSubmit} className="create-post-form">
          <div className="form-section">
            <h3>{t("createPost.imageSection")}</h3>
            <div className="image-upload-area">
              {imagePreview ? (
                <div className="image-preview-container">
                  <img src={imagePreview} alt={t("createPost.imagePreviewAlt")} />
                  <button
                    type="button"
                    className="change-image-button"
                    onClick={() => {
                      setImageFile(null);
                      setImagePreview(null);
                    }}
                  >
                    {t("common.actions.changeImage")}
                  </button>
                </div>
              ) : (
                <label className="image-upload-label">
                  <svg viewBox="0 0 24 24" aria-hidden="true">
                    <path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0 16H5V5h14v14zm-5.04-6.71l-2.75-3.54-4.04 5.25h12l-3.21-4.21z" />
                  </svg>
                  <p>{t("createPost.uploadPrompt")}</p>
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

          <div className="form-section">
            <h3>{t("createPost.detailsSection")}</h3>

            <div className="form-group">
              <label htmlFor="title">{t("createPost.titleLabel")}</label>
              <input
                id="title"
                type="text"
                name="title"
                className="auth-input"
                value={formData.title}
                onChange={handleInputChange}
                placeholder={t("createPost.titlePlaceholder")}
                disabled={loading}
              />
            </div>

            <div className="form-group">
              <label htmlFor="description">{t("createPost.descriptionLabel")}</label>
              <textarea
                id="description"
                name="description"
                className="auth-input form-textarea"
                value={formData.description}
                onChange={handleInputChange}
                placeholder={t("createPost.descriptionPlaceholder")}
                rows="3"
                disabled={loading}
              />
            </div>

            <div className="form-row">
              <div className="form-group">
                <label htmlFor="amount">{t("createPost.priceLabel")}</label>
                <input
                  id="amount"
                  type="number"
                  name="amount"
                  className="auth-input"
                  value={formData.amount}
                  onChange={handleInputChange}
                  placeholder={t("createPost.amountPlaceholder")}
                  step="0.01"
                  min="0"
                  disabled={loading}
                />
              </div>

              <div className="form-group">
                <label htmlFor="quantity">{t("createPost.quantityLabel")}</label>
                <input
                  id="quantity"
                  type="number"
                  name="quantity"
                  className="auth-input"
                  value={formData.quantity}
                  onChange={handleInputChange}
                  placeholder={t("createPost.quantityPlaceholder")}
                  min="1"
                  disabled={loading}
                />
              </div>
            </div>

            <div className="form-row">
              <div className="form-group">
                <label htmlFor="category">{t("createPost.categoryLabel")}</label>
                <select
                  id="category"
                  name="category"
                  className="auth-input"
                  value={formData.category}
                  onChange={handleInputChange}
                  disabled={loading}
                >
                  <option value="Indoor">{translateCategory("Indoor")}</option>
                  <option value="Outdoor">{translateCategory("Outdoor")}</option>
                  <option value="Succulent">{translateCategory("Succulent")}</option>
                  <option value="Flowering">{translateCategory("Flowering")}</option>
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
                  {t("createPost.statusLabel")}
                </label>
              </div>
            </div>
          </div>

          {error && <div className="auth-error">{error}</div>}
          {success && (
            <div className="auth-notice">
              {t("createPost.success")}
            </div>
          )}
        </form>

        <div className="form-actions">
          <button
            type="button"
            className="secondary-button"
            onClick={onClose}
            disabled={loading}
          >
            {t("common.actions.cancel")}
          </button>
          <button
            type="submit"
            className="primary-button"
            disabled={loading || uploadingImage}
            onClick={handleSubmit}
          >
            {loading
              ? t("common.actions.creating")
              : uploadingImage
                ? t("common.actions.uploadImage")
                : t("common.actions.createPost")}
          </button>
        </div>
      </div>
    </div>
  );
}
