import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './Header.css';

export default function Header({ onSearch }) {
  const { isAuthenticated, user, logout } = useAuth();
  const navigate = useNavigate();

  function handleLogout() {
    logout();
    navigate('/');
  }

  function handleSearch(e) {
    e.preventDefault();
    const query = e.target.elements.search.value.trim();
    if (onSearch) onSearch(query);
  }

  const initials = user?.name
    ? user.name.split(' ').map(n => n[0]).join('').slice(0, 2).toUpperCase()
    : '?';

  return (
    <header className="header">
      <div className="header__inner container">
        <button className="header__logo" onClick={() => navigate(isAuthenticated ? '/home' : '/')}>
          <span className="header__logo-mark">D</span>
          <span className="header__logo-text">Dawgs <span>Marketplace</span></span>
        </button>

        {isAuthenticated && (
          <form className="header__search" onSubmit={handleSearch}>
            <input
              name="search"
              type="text"
              placeholder="Search listings..."
              className="header__search-input"
            />
            <button type="submit" className="header__search-btn">Search</button>
          </form>
        )}

        <nav className="header__nav">
          {isAuthenticated ? (
            <>
              <div className="header__avatar">{initials}</div>
              <button className="header__nav-btn header__nav-btn--primary" onClick={() => navigate('/create')}>
                + List Item
              </button>
              <button className="header__nav-btn" onClick={() => navigate('/my-listings')}>
                My Listings
              </button>
              <button className="header__nav-btn" onClick={() => navigate('/home')}>
                Browse
              </button>
              <button className="header__nav-btn" onClick={handleLogout}>
                Sign Out
              </button>
            </>
          ) : (
            <span className="header__tagline">UW Students Only</span>
          )}
        </nav>
      </div>
    </header>
  );
}
