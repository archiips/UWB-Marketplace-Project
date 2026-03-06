import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import './CreateListing.css';

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

export default function CreateListing() {
  const navigate = useNavigate();
  const [form, setForm] = useState({ title: '', description: '', price: '', contactInfo: '' });
  const [errors, setErrors] = useState({});
  const [submitting, setSubmitting] = useState(false);
  const [serverError, setServerError] = useState(null);

  const canSubmit =
    form.title.trim() &&
    form.description.trim() &&
    form.price && !isNaN(form.price) && Number(form.price) > 0 &&
    form.contactInfo.trim();

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

  async function handleSubmit(e) {
    e.preventDefault();
    const errs = validate();
    if (Object.keys(errs).length > 0) { setErrors(errs); return; }
    setSubmitting(true);
    setServerError(null);
    try {
      await axios.post(`${API_URL}/api/listings`, {
        title: form.title.trim(),
        description: form.description.trim(),
        price: parseFloat(form.price),
        contactInfo: form.contactInfo.trim(),
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
          <p className="create-card__subtitle">All fields are required</p>
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
              id="description" name="description" rows={5}
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
              {submitting ? (
                <><span className="create-btn__spinner" />Posting…</>
              ) : 'Post Listing'}
            </button>
          </div>

        </form>
      </div>
    </main>
  );
}
