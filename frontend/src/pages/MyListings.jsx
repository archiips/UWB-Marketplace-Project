import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { useAuth } from '../context/AuthContext';
import './MyListings.css';

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

function SkeletonRow() {
  return (
    <div className="mylistings__item mylistings__item--skeleton">
      <div className="mylistings__img-wrap skeleton-block" />
      <div className="mylistings__info">
        <div className="skeleton-line skeleton-line--title" />
        <div className="skeleton-line skeleton-line--price" />
        <div className="skeleton-line skeleton-line--desc" />
      </div>
    </div>
  );
}

export default function MyListings() {
  const [listings, setListings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const navigate = useNavigate();
  const { token } = useAuth();

  function authHeaders() {
    const jwt = token || localStorage.getItem('jwt');
    return jwt ? { Authorization: `Bearer ${jwt}` } : {};
  }

  function loadListings() {
    setLoading(true);
    setError(null);
    axios.get(`${API_URL}/api/listings/my-listings`, { headers: authHeaders() })
      .then(res => setListings(res.data))
      .catch(err => {
        const status = err.response?.status;
        const msg = status ? `Error ${status}` : 'Network error (is the backend running?)';
        setError(`Could not load your listings. ${msg}`);
        console.error('MyListings error:', status, err.response?.data);
      })
      .finally(() => setLoading(false));
  }

  useEffect(() => { loadListings(); }, []); // eslint-disable-line

  async function handleDelete(id) {
    if (!window.confirm('Delete this listing?')) return;
    try {
      await axios.delete(`${API_URL}/api/listings/${id}`, { headers: authHeaders() });
      setListings(prev => prev.filter(l => l.id !== id));
    } catch {
      alert('Failed to delete listing.');
    }
  }

  async function handleMarkSold(id) {
    try {
      const res = await axios.put(
        `${API_URL}/api/listings/${id}`,
        { sold: true },
        { headers: authHeaders() }
      );
      setListings(prev => prev.map(l => l.id === id ? res.data : l));
    } catch {
      alert('Failed to mark as sold.');
    }
  }

  return (
    <main className="mylistings container--body container">

      <div className="mylistings__header">
        <div>
          <h1 className="mylistings__title">My Listings</h1>
          {!loading && !error && (
            <p className="mylistings__subtitle">
              {listings.length} item{listings.length !== 1 ? 's' : ''}
            </p>
          )}
        </div>
        <button className="mylistings__new-btn" onClick={() => navigate('/create')}>
          + New Listing
        </button>
      </div>

      {loading && (
        <div className="mylistings__list">
          {Array.from({ length: 3 }).map((_, i) => <SkeletonRow key={i} />)}
        </div>
      )}

      {error && !loading && (
        <div className="mylistings__empty-state">
          <p className="mylistings__error">{error}</p>
          <button className="mylistings__retry" onClick={loadListings}>Retry</button>
        </div>
      )}

      {!loading && !error && listings.length === 0 && (
        <div className="mylistings__empty-state">
          <div className="mylistings__empty-icon">
            <svg viewBox="0 0 48 48" fill="none" stroke="currentColor" strokeWidth="1.2">
              <rect x="8" y="10" width="32" height="28" rx="3" />
              <path d="M16 18h16M16 24h10" strokeLinecap="round" />
            </svg>
          </div>
          <p className="mylistings__empty-title">No listings yet</p>
          <p className="mylistings__empty-sub">Post your first item for sale</p>
          <button className="mylistings__new-btn" style={{ marginTop: 8 }} onClick={() => navigate('/create')}>
            + Create Listing
          </button>
        </div>
      )}

      {!loading && !error && listings.length > 0 && (
        <div className="mylistings__list">
          {listings.map(item => (
            <div
              key={item.id}
              className={`mylistings__item${item.sold ? ' mylistings__item--sold' : ''}`}
            >
              <div className="mylistings__img-wrap">
                {item.imageUrl
                  ? <img src={item.imageUrl} alt={item.title} className="mylistings__img" />
                  : (
                    <div className="mylistings__img-placeholder">
                      <svg viewBox="0 0 48 48" fill="none" stroke="currentColor" strokeWidth="1.2">
                        <rect x="6" y="6" width="36" height="36" rx="4" />
                        <circle cx="17" cy="19" r="4" />
                        <path d="M6 34l10-10 8 8 6-6 12 12" />
                      </svg>
                      <span>No image</span>
                    </div>
                  )
                }
              </div>

              <div className="mylistings__info">
                <p className="mylistings__item-title">{item.title}</p>
                <p className="mylistings__item-price">${Number(item.price).toFixed(2)}</p>
                {item.description && (
                  <p className="mylistings__item-desc">{item.description}</p>
                )}
                {item.sold && <span className="mylistings__sold-tag">Sold</span>}
              </div>

              <div className="mylistings__actions">
                <button
                  className="mylistings__btn mylistings__btn--view"
                  onClick={() => navigate(`/listing/${item.id}`)}
                >
                  View
                </button>
                {!item.sold && (
                  <button
                    className="mylistings__btn mylistings__btn--sold"
                    onClick={() => handleMarkSold(item.id)}
                  >
                    Mark Sold
                  </button>
                )}
                <button
                  className="mylistings__btn mylistings__btn--danger"
                  onClick={() => handleDelete(item.id)}
                >
                  Delete
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

    </main>
  );
}
