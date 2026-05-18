import { useState, useRef } from "react";
import { useI18n } from "./i18n/I18nContext";

const BUCKET_NAME = "icons";
const AVATAR_SIZE = 256;

export default function EditProfile({ user, onSaveSuccess, onCancel }) {
  const { t, translateError } = useI18n();
  const [username, setUsername] = useState(user.username || "");
  const [phone, setPhone] = useState(user.phone || "");
  const [avatarUrl, setAvatarUrl] = useState(user.avatarUrl || null);
  const [avatarFile, setAvatarFile] = useState(null);
  const [avatarPreview, setAvatarPreview] = useState(user.avatarUrl || null);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [loading, setLoading] = useState(false);
  const [uploadingImage, setUploadingImage] = useState(false);
  const fileInputRef = useRef(null);

  const resizeImage = (file) => {
    return new Promise((resolve, reject) => {
      const img = new Image();
      img.onload = () => {
        const canvas = document.createElement("canvas");
        canvas.width = AVATAR_SIZE;
        canvas.height = AVATAR_SIZE;
        const ctx = canvas.getContext("2d");

        // Center-crop to square
        const minDim = Math.min(img.width, img.height);
        const sx = (img.width - minDim) / 2;
        const sy = (img.height - minDim) / 2;

        ctx.drawImage(
          img,
          sx, sy, minDim, minDim,
          0, 0, AVATAR_SIZE, AVATAR_SIZE
        );

        canvas.toBlob(
          (blob) => {
            if (blob) resolve(blob);
            else reject(new Error("Canvas toBlob failed"));
          },
          "image/jpeg",
          0.9
        );
      };
      img.onerror = reject;
      img.src = URL.createObjectURL(file);
    });
  };

  const handleAvatarChange = async (e) => {
    const file = e.target.files[0];
    if (!file) return;

    if (!file.type.startsWith("image/")) {
      setError(t("errors.imageFileInvalid"));
      return;
    }

    setError("");
    setUploadingImage(true);

    try {
      const resizedBlob = await resizeImage(file);
      setAvatarFile(resizedBlob);
      setAvatarPreview(URL.createObjectURL(resizedBlob));
    } catch (err) {
      console.error("Image resize error:", err);
      setError(t("errors.generic"));
    } finally {
      setUploadingImage(false);
    }
  };

  const uploadAvatar = async (blob) => {
    const SUPABASE_URL = process.env.REACT_APP_SUPABASE_URL;
    const SUPABASE_ANON_KEY = process.env.REACT_APP_SUPABASE_ANON_KEY;

    if (!SUPABASE_URL || !SUPABASE_ANON_KEY) {
      throw new Error(t("errors.supabaseMissing"));
    }

    const timestamp = Date.now();
    const filePath = `${user.id}-${timestamp}.jpg`;

    const response = await fetch(
      `${SUPABASE_URL}/storage/v1/object/${BUCKET_NAME}/${filePath}`,
      {
        method: "POST",
        headers: {
          apikey: SUPABASE_ANON_KEY,
          Authorization: `Bearer ${user.accessToken}`,
          "Content-Type": "image/jpeg",
          "x-upsert": "false",
        },
        body: blob,
      }
    );

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`Upload failed (${response.status}): ${errorText}`);
    }

    return `${SUPABASE_URL}/storage/v1/object/public/${BUCKET_NAME}/${filePath}`;
  };

  const updateAuthMetadata = async (url) => {
    const SUPABASE_URL = process.env.REACT_APP_SUPABASE_URL;
    const SUPABASE_ANON_KEY = process.env.REACT_APP_SUPABASE_ANON_KEY;

    const res = await fetch(`${SUPABASE_URL}/auth/v1/user`, {
      method: "PUT",
      headers: {
        apikey: SUPABASE_ANON_KEY,
        Authorization: `Bearer ${user.accessToken}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        data: { avatar_url: url },
      }),
    });

    if (!res.ok) {
      const text = await res.text().catch(() => "");
      throw new Error(text);
    }
  };

  const handleSave = async () => {
    setError("");
    setSuccess("");
    setLoading(true);

    let newAvatarUrl = avatarUrl;

    try {
      // Upload new avatar if selected
      if (avatarFile) {
        newAvatarUrl = await uploadAvatar(avatarFile);
        await updateAuthMetadata(newAvatarUrl);
        setAvatarUrl(newAvatarUrl);
        setAvatarFile(null);
      }

      // Update profile fields via backend
      const res = await fetch(`/api/users/${user.id}`, {
        method: "PATCH",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username, phone }),
      });

      if (!res.ok) {
        const bodyText = await res.text().catch(() => "");
        setError(translateError(bodyText, "errors.generic"));
        setLoading(false);
        return;
      }

      const updated = await res.json();
      setSuccess(t("editProfile.success"));
      onSaveSuccess({ ...user, ...updated, avatarUrl: newAvatarUrl });
    } catch (err) {
      console.error("Update profile error:", err);
      setError(t("errors.connection"));
    } finally {
      setLoading(false);
    }
  };

  const triggerFileSelect = () => {
    fileInputRef.current?.click();
  };

  return (
    <main className="auth-shell">
      <section className="auth-card">
        <span className="auth-kicker">{t("common.brand")}</span>
        <h1>{t("editProfile.title")}</h1>
        <p className="auth-copy">{t("editProfile.subtitle")}</p>

        {/* Avatar Upload Area */}
        <div className="avatar-upload-wrapper">
          <div
            className="avatar-preview"
            onClick={triggerFileSelect}
            role="button"
            tabIndex={0}
            aria-label={t("editProfile.changePhoto")}
          >
            {avatarPreview ? (
              <img src={avatarPreview} alt={t("editProfile.avatarAlt")} />
            ) : (
              <svg viewBox="0 0 24 24" aria-hidden="true">
                <path d="M12 12a4.25 4.25 0 1 0-4.25-4.25A4.26 4.26 0 0 0 12 12Zm0 2.25c-3.9 0-7 2.01-7 4.5V20h14v-1.25c0-2.49-3.1-4.5-7-4.5Z" />
              </svg>
            )}
            <div className="avatar-overlay">
              <span>{uploadingImage ? t("editProfile.resizing") : t("editProfile.changePhoto")}</span>
            </div>
          </div>
          <input
            ref={fileInputRef}
            type="file"
            accept="image/*"
            onChange={handleAvatarChange}
            style={{ display: "none" }}
          />
        </div>

        <div className="auth-fields">
          <input
            className="auth-input"
            placeholder={t("common.labels.username")}
            value={username}
            onChange={(e) => setUsername(e.target.value)}
          />
          <input
            className="auth-input"
            placeholder={t("common.labels.phone")}
            value={phone}
            onChange={(e) => setPhone(e.target.value)}
          />
          <input
            className="auth-input"
            placeholder={t("common.labels.email")}
            type="email"
            value={user.email || ""}
            disabled
            style={{ opacity: 0.7, cursor: "not-allowed" }}
          />
        </div>

        <div className="auth-actions">
          <button
            className="primary-button"
            onClick={handleSave}
            disabled={loading || uploadingImage}
          >
            {loading ? t("editProfile.saving") : t("editProfile.save")}
          </button>
          <button className="secondary-button" onClick={onCancel}>
            {t("editProfile.backToDashboard")}
          </button>
        </div>

        {success && <p className="auth-notice">{success}</p>}
        {error && <p className="auth-error">{error}</p>}
      </section>
    </main>
  );
}
