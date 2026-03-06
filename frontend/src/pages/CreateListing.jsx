import { useState, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import './CreateListing.css';

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';
const CLOUD_NAME = import.meta.env.VITE_CLOUDINARY_CLOUD_NAME;
const UPLOAD_PRESET = import.meta.env.VITE_CLOUDINARY_UPLOAD_PRESET;

const MAX_IMAGES = 6;

async function uploadToCloudinary(file) {
  const data = new FormData();
  data.append('file', file);
  data.append('upload_preset', UPLOAD_PRESET);
  data.append('folder', 'listings');
  const res = await fetch(
    `https://api.cloudinary.com/v1_1/${CLOUD_NAME}/image/upload`,
    { method: 'POST', body: data }
  );
  const json = await res.json();
  if (!res.ok) throw new Error(json.error?.message || 'Upload failed');
  return json.secure_url;
}

export default function CreateListing() {
  const navigate = useNavigate();
  const fileInputRef = useRef(null);

  const [form, setForm] = useState({ title: '', description: '', price: '', contactInfo: '' });
  const [images, setImages] = useState([]); // [{ preview, url, uploading, error }]
  const [errors, setErrors] = useState({});
  const [submitting, setSubmitting] = useState(false);
  const [serverError, setServerError] = useState(null);

  const anyUploading = images.some(img => img.uploading);
  const canSubmit =
    form.title.trim() &&
    form.description.trim() &&
    form.price && !isNaN(form.price) && Number(form.price) > 0 &&
    form.contactInfo.trim() &&
    !anyUploading;

  function validate() {
    const e = {};
    if (!form.title.trim()) e.title = 'Title is required.';
    if (!form.description.trim()) e.description = 'Description is required.';
    if (!form.price || isNaN(form.price) || Number(form.price) <= 0)
      e.price = 'Enter a valid price greater than $0.';
    if (!form.contactInfo.trim()) e.contactInfo = 'Contact info is required.';
    return e;
  }

  function handleChange(e) {
    setForm(prev => ({ ...prev, [e.target.name]: e.target.value }));
    setErrors(prev => ({ ...prev, [e.target.name]: undefined }));
  }

  async function handleFilesSelected(e) {
    const files = Array.from(e.target.files);
    if (!files.length) return;

    const slots = Math.min(files.length, MAX_IMAGES - images.length);
    const newImages = files.slice(0, slots).map(file => ({
      id: Math.random().toString(36).slice(2),
      preview: URL.createObjectURL(file),
      url: null,
      uploading: true,
      error: false,
    }));

    setImages(prev => [...prev, ...newImages]);
    // Reset input so same file can be re-selected
    e.target.value = '';

    await Promise.all(newImages.map(async (img, i) => {
      try {
        const url = await uploadToCloudinary(files[i]);
        setImages(prev => prev.map(p =>
          p.id === img.id ? { ...p, url, uploading: false } : p
        ));
      } catch {
        setImages(prev => prev.map(p =>
          p.id === img.id ? { ...p, uploading: false, error: true } : p
        ));
      }
    }));
  }

  function removeImage(id) {
    setImages(prev => prev.filter(img => img.id !== id));
  }

  async function handleSubmit(e) {
    e.preventDefault();
    const errs = validate();
    if (Object.keys(errs).length > 0) { setErrors(errs); return; }

    const uploadedUrls = images.filter(img => img.url).map(img => img.url);

    setSubmitting(true);
    setServerError(null);
    try {
      await axios.post(`${API_URL}/api/listings`, {
        title: form.title.trim(),
        description: form.description.trim(),
        price: parseFloat(form.price),
        contactInfo: form.contactInfo.trim(),
        // Store as JSON array string; fallback to single URL or null
        imageUrl: uploadedUrls.length > 0 ? JSON.stringify(uploadedUrls) : null,
      });
      navigate('/home');
    } catch (err) {
      setServerError(err.response?.data?.message || 'Failed to create listing. Please try again.');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <main className="create-page">
      <div className="create-card">

        <div className="create-card__header">
          <h1 className="create-card__title">Create a Listing</h1>
          <p className="create-card__subtitle">All fields required · Photos optional</p>
        </div>

        <div className="create-card__divider" />

        {serverError && <div className="create-error">{serverError}</div>}

        <form className="create-form" onSubmit={handleSubmit} noValidate>

          {/* Title + Price */}
          <div className="create-form__row">
            <div className="create-form__group">
              <label htmlFor="title" className="create-form__label">
                Title <span className="create-form__required">*</span>
              </label>
              <input
                id="title" name="title" type="text"
                className={`create-form__input${errors.title ? ' create-form__input--error' : ''}`}
                value={form.title} onChange={handleChange}
                placeholder="What are you selling?"
              />
              {errors.title && <span className="create-form__error">{errors.title}</span>}
            </div>

            <div className="create-form__group create-form__group--sm">
              <label htmlFor="price" className="create-form__label">
                Price ($) <span className="create-form__required">*</span>
              </label>
              <input
                id="price" name="price" type="number"
                min="0.01" step="0.01"
                className={`create-form__input${errors.price ? ' create-form__input--error' : ''}`}
                value={form.price} onChange={handleChange}
                placeholder="0.00"
              />
              {errors.price && <span className="create-form__error">{errors.price}</span>}
            </div>
          </div>

          {/* Description */}
          <div className="create-form__group">
            <label htmlFor="description" className="create-form__label">
              Description <span className="create-form__required">*</span>
            </label>
            <textarea
              id="description" name="description" rows={4}
              className={`create-form__input create-form__textarea${errors.description ? ' create-form__input--error' : ''}`}
              value={form.description} onChange={handleChange}
              placeholder="Describe your item - condition, size, details..."
            />
            {errors.description && <span className="create-form__error">{errors.description}</span>}
          </div>

          {/* Contact info */}
          <div className="create-form__group">
            <label htmlFor="contactInfo" className="create-form__label">
              Contact Info <span className="create-form__required">*</span>
              <span className="create-form__label-hint"> - how buyers can reach you</span>
            </label>
            <input
              id="contactInfo" name="contactInfo" type="text"
              className={`create-form__input${errors.contactInfo ? ' create-form__input--error' : ''}`}
              value={form.contactInfo} onChange={handleChange}
              placeholder="Email, phone, Discord, Instagram..."
            />
            {errors.contactInfo && <span className="create-form__error">{errors.contactInfo}</span>}
          </div>

          {/* Photos */}
          <div className="create-form__group">
            <label className="create-form__label">
              Photos
              <span className="create-form__label-hint"> - up to {MAX_IMAGES} images</span>
            </label>

            <div className="create-upload-grid">
              {images.map(img => (
                <div key={img.id} className="create-upload-thumb">
                  <img src={img.preview} alt="" className="create-upload-thumb__img" />
                  {img.uploading && (
                    <div className="create-upload-thumb__overlay">
                      <span className="create-btn__spinner create-btn__spinner--light" />
                    </div>
                  )}
                  {img.error && (
                    <div className="create-upload-thumb__overlay create-upload-thumb__overlay--error">
                      Failed
                    </div>
                  )}
                  {!img.uploading && (
                    <button
                      type="button"
                      className="create-upload-thumb__remove"
                      onClick={() => removeImage(img.id)}
                    >
                      ×
                    </button>
                  )}
                </div>
              ))}

              {images.length < MAX_IMAGES && (
                <button
                  type="button"
                  className="create-upload-add"
                  onClick={() => fileInputRef.current?.click()}
                >
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <path d="M12 5v14M5 12h14" strokeLinecap="round" />
                  </svg>
                  <span>{images.length === 0 ? 'Add photos' : 'Add more'}</span>
                </button>
              )}
            </div>

            <input
              ref={fileInputRef}
              type="file"
              accept="image/*"
              multiple
              style={{ display: 'none' }}
              onChange={handleFilesSelected}
            />
          </div>

          {/* Actions */}
          <div className="create-form__actions">
            <button type="button" className="create-btn create-btn--cancel" onClick={() => navigate('/home')}>
              Cancel
            </button>
            <button
              type="submit"
              className="create-btn create-btn--submit"
              disabled={submitting || !canSubmit}
              title={!canSubmit ? 'Please fill in all required fields' : ''}
            >
              {submitting
                ? <><span className="create-btn__spinner" />Posting…</>
                : 'Post Listing'
              }
            </button>
          </div>

        </form>
      </div>
    </main>
  );
}
