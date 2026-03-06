import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import './Home.css';

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

function SkeletonCard() {
  return (
    <div className="listing-card listing-card--skeleton">
      <div className="listing-card__img-wrap skeleton-block" />
      <div className="listing-card__body">
        <div className="skeleton-line skeleton-line--title" />
        <div className="skeleton-line skeleton-line--desc" />
        <div className="skeleton-line skeleton-line--price" />
      </div>
    </div>
  );
}

function ImagePlaceholder() {
  return (
    <div className="listing-card__img-placeholder">
      <svg viewBox="0 0 48 48" fill="none" stroke="currentColor" strokeWidth="1.2">
        <rect x="6" y="6" width="36" height="36" rx="4" />
        <circle cx="17" cy="19" r="4" />
        <path d="M6 34l10-10 8 8 6-6 12 12" />
      </svg>
      <span>No image</span>
    </div>
  );
}

const PAGE_SIZE = 16;

function getFirstImage(imageUrl) {
  if (!imageUrl) return null;
  try {
    const parsed = JSON.parse(imageUrl);
    return Array.isArray(parsed) && parsed.length > 0 ? parsed[0] : imageUrl;
  } catch {
    return imageUrl;
  }
}

export default function Home({ searchQuery }) {
  const [listings, setListings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [page, setPage] = useState(1);
  const navigate = useNavigate();

  useEffect(() => {
    setLoading(true);
    setError(null);
    const url = searchQuery
      ? `${API_URL}/api/listings?search=${encodeURIComponent(searchQuery)}`
      : `${API_URL}/api/listings`;

    axios.get(url)
      .then(res => { setListings(res.data); setPage(1); })
      .catch(() => setError('Could not load listings. Please try again.'))
      .finally(() => setLoading(false));
  }, [searchQuery]);

  if (loading) {
    return (
      <main className="home">
        <div className="home__heading">
          <div className="skeleton-line skeleton-line--heading" />
        </div>
        <div className="listing-grid">
          {Array.from({ length: 8 }).map((_, i) => <SkeletonCard key={i} />)}
        </div>
      </main>
    );
  }

  if (error) {
    return (
      <main className="home">
        <div className="home__empty-state">
          <div className="home__empty-icon">
            <svg viewBox="0 0 48 48" fill="none" stroke="currentColor" strokeWidth="1.5">
              <circle cx="24" cy="24" r="20" />
              <path d="M24 14v12M24 32v2" strokeLinecap="round" />
            </svg>
          </div>
          <p className="home__empty-title">Something went wrong</p>
          <p className="home__empty-sub">{error}</p>
        </div>
      </main>
    );
  }

  return (
    <main className="home">
      {searchQuery ? (
        <div className="home__heading">
          <h2>Search results</h2>
          <span className="home__heading-count">
            {listings.length} {listings.length === 1 ? 'result' : 'results'} for "{searchQuery}"
          </span>
        </div>
      ) : (
        <div className="home__heading">
          <h2>All Listings</h2>
          {listings.length > 0 && (
            <span className="home__heading-count">{listings.length} items</span>
          )}
        </div>
      )}

      {listings.length === 0 ? (
        <div className="home__empty-state">
          <div className="home__empty-icon">
            <svg viewBox="0 0 48 48" fill="none" stroke="currentColor" strokeWidth="1.2">
              <rect x="8" y="10" width="32" height="28" rx="3" />
              <path d="M16 18h16M16 24h10" strokeLinecap="round" />
            </svg>
          </div>
          <p className="home__empty-title">
            {searchQuery ? `No results for "${searchQuery}"` : 'No listings yet'}
          </p>
          <p className="home__empty-sub">
            {searchQuery ? 'Try a different search term.' : 'Be the first to post something!'}
          </p>
        </div>
      ) : (() => {
        const totalPages = Math.ceil(listings.length / PAGE_SIZE);
        const pageItems = listings.slice((page - 1) * PAGE_SIZE, page * PAGE_SIZE);
        return (
          <>
            <div className="listing-grid">
              {pageItems.map(item => (
                <button
                  key={item.id}
                  className={`listing-card${item.sold ? ' listing-card--sold' : ''}`}
                  onClick={() => navigate(`/listing/${item.id}`)}
                >
                  <div className="listing-card__img-wrap">
                    {getFirstImage(item.imageUrl)
                      ? <img src={getFirstImage(item.imageUrl)} alt={item.title} className="listing-card__img" />
                      : <ImagePlaceholder />
                    }
                    {item.sold && <span className="listing-card__sold-badge">Sold</span>}
                  </div>
                  <div className="listing-card__body">
                    <p className="listing-card__title">{item.title}</p>
                    {item.description && (
                      <p className="listing-card__desc">{item.description}</p>
                    )}
                    <div className="listing-card__footer">
                      <span className="listing-card__price">${Number(item.price).toFixed(2)}</span>
                      <span className="listing-card__cta">View &rarr;</span>
                    </div>
                  </div>
                </button>
              ))}
            </div>

            {totalPages > 1 && (
              <div className="home__pagination">
                <button
                  className="home__page-btn"
                  onClick={() => { setPage(p => p - 1); window.scrollTo(0, 0); }}
                  disabled={page === 1}
                >
                  &larr; Prev
                </button>
                <span className="home__page-info">
                  Page {page} of {totalPages}
                </span>
                <button
                  className="home__page-btn"
                  onClick={() => { setPage(p => p + 1); window.scrollTo(0, 0); }}
                  disabled={page === totalPages}
                >
                  Next &rarr;
                </button>
              </div>
            )}
          </>
        );
      })()}
    </main>
  );
}
