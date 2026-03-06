import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
import './ListingDetail.css';

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

export default function ListingDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [listing, setListing] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    axios.get(`${API_URL}/api/listings/${id}`)
      .then(res => setListing(res.data))
      .catch(() => setError('Listing not found.'))
      .finally(() => setLoading(false));
  }, [id]);

  if (loading) return <main className="detail container--body container"><p>Loading...</p></main>;
  if (error) return <main className="detail container--body container"><p className="detail__error">{error}</p></main>;

  return (
    <main className="detail container--body container">
      <button className="detail__back" onClick={() => navigate('/home')}>
        &larr; Back to listings
      </button>
      <div className="detail__card">
        <div className="detail__img-wrap">
          {listing.imageUrl ? (
            <img src={listing.imageUrl} alt={listing.title} className="detail__img" />
          ) : (
            <div className="detail__img-placeholder">No Image</div>
          )}
          {listing.sold && <span className="detail__sold-banner">SOLD</span>}
        </div>
        <div className="detail__info">
          <h1 className="detail__title">{listing.title}</h1>
          <p className="detail__price">${Number(listing.price).toFixed(2)}</p>
          <p className="detail__desc">{listing.description}</p>
          <p className="detail__date">
            Posted {new Date(listing.createdAt).toLocaleDateString()}
          </p>
          {!listing.sold && listing.contactInfo && (
            <div className="detail__contact-box">
              <p className="detail__contact-label">Contact Seller</p>
              <p className="detail__contact-value">{listing.contactInfo}</p>
            </div>
          )}
        </div>
      </div>
    </main>
  );
}
