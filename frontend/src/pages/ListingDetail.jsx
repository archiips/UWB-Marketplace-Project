import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Carousel } from '@ark-ui/react/carousel';
import axios from 'axios';
import './ListingDetail.css';

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

function parseImages(imageUrl) {
  if (!imageUrl) return [];
  try {
    const parsed = JSON.parse(imageUrl);
    return Array.isArray(parsed) ? parsed : [imageUrl];
  } catch {
    return [imageUrl];
  }
}

function ImageCarousel({ images }) {
  const [current, setCurrent] = useState(0);

  if (images.length === 0) {
    return (
      <div className="detail__img-placeholder">
        <svg viewBox="0 0 48 48" fill="none" stroke="currentColor" strokeWidth="1.2">
          <rect x="6" y="6" width="36" height="36" rx="4" />
          <circle cx="17" cy="19" r="4" />
          <path d="M6 34l10-10 8 8 6-6 12 12" />
        </svg>
        <span>No photos</span>
      </div>
    );
  }

  if (images.length === 1) {
    return (
      <div className="detail__single-img-wrap">
        <img src={images[0]} alt="Listing" className="detail__single-img" />
      </div>
    );
  }

  return (
    <Carousel.Root
      defaultPage={0}
      slideCount={images.length}
      loop
      className="detail__carousel"
      onPageChange={e => setCurrent(e.page)}
    >
      <div className="detail__carousel-main">
        <Carousel.ItemGroup className="detail__carousel-track">
          {images.map((src, i) => (
            <Carousel.Item key={i} index={i} className="detail__carousel-slide">
              <img src={src} alt={`Photo ${i + 1}`} className="detail__carousel-img" />
            </Carousel.Item>
          ))}
        </Carousel.ItemGroup>
        <Carousel.PrevTrigger className="detail__carousel-btn detail__carousel-btn--prev">‹</Carousel.PrevTrigger>
        <Carousel.NextTrigger className="detail__carousel-btn detail__carousel-btn--next">›</Carousel.NextTrigger>
        <div className="detail__carousel-counter">{current + 1} / {images.length}</div>
      </div>
      <div className="detail__carousel-thumbs">
        {images.map((src, i) => (
          <Carousel.Indicator key={i} index={i} className="detail__carousel-thumb">
            <img src={src} alt={`Thumbnail ${i + 1}`} />
          </Carousel.Indicator>
        ))}
      </div>
    </Carousel.Root>
  );
}

export default function ListingDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [listing, setListing] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    axios.get(`${API_URL}/api/listings/${id}`)
      .then(res => setListing(res.data))
      .catch(() => setError('Listing not found or could not be loaded.'));
  }, [id]);

  if (error) return (
    <main className="detail-page container--body container">
      <button className="detail__back" onClick={() => navigate(-1)}>← Back</button>
      <p className="detail__error">{error}</p>
    </main>
  );

  if (!listing) return (
    <main className="detail-page container--body container">
      <div className="detail__skeleton">
        <div className="detail__skeleton-img skeleton-block" />
        <div className="detail__skeleton-body">
          <div className="skeleton-line skeleton-line--title" />
          <div className="skeleton-line skeleton-line--price" />
          <div className="skeleton-line skeleton-line--desc" />
        </div>
      </div>
    </main>
  );

  const images = parseImages(listing.imageUrl);
  const formattedDate = listing.createdAt
    ? new Date(listing.createdAt).toLocaleDateString('en-US', { month: 'long', day: 'numeric', year: 'numeric' })
    : null;

  return (
    <main className="detail-page container--body container">
      <button className="detail__back" onClick={() => navigate(-1)}>← Back to listings</button>

      <div className="detail__card">
        <div className="detail__media">
          <ImageCarousel images={images} />
          {listing.sold && <span className="detail__sold-banner">SOLD</span>}
        </div>

        <div className="detail__info">
          <div className="detail__info-top">
            <h1 className="detail__title">{listing.title}</h1>
            <p className="detail__price">${Number(listing.price).toFixed(2)}</p>
          </div>

          <div className="detail__divider" />

          <div className="detail__section">
            <p className="detail__section-label">Description</p>
            <p className="detail__desc">{listing.description}</p>
          </div>

          {listing.contactInfo && !listing.sold && (
            <div className="detail__contact-box">
              <p className="detail__contact-label">Contact Seller</p>
              <p className="detail__contact-value">{listing.contactInfo}</p>
            </div>
          )}

          {listing.sold && (
            <div className="detail__sold-notice">This item has been sold.</div>
          )}

          {formattedDate && <p className="detail__date">Listed on {formattedDate}</p>}
        </div>
      </div>
    </main>
  );
}
