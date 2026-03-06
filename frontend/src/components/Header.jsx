import { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './Header.css';

export default function Header({ onSearch }) {
  const { isAuthenticated, user, logout } = useAuth();
  const navigate = useNavigate();
  const [menuOpen, setMenuOpen] = useState(false);
  const menuRef = useRef(null);

  function handleLogout() {
    logout();
    navigate('/');
  }

  function handleSearch(e) {
    e.preventDefault();
    const query = e.target.elements.search.value.trim();
    if (onSearch) onSearch(query);
  }

  function navTo(path) {
    setMenuOpen(false);
    navigate(path);
  }

  // Close menu on outside click
  useEffect(() => {
    function handleClick(e) {
      if (menuRef.current && !menuRef.current.contains(e.target)) {
        setMenuOpen(false);
      }
    }
    document.addEventListener('mousedown', handleClick);
    return () => document.removeEventListener('mousedown', handleClick);
  }, []);

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
              <div className="header__avatar header__avatar--desktop">{initials}</div>
              <button className="header__nav-btn header__nav-btn--primary header__nav-btn--desktop" onClick={() => navigate('/create')}>
                + List Item
              </button>
              <button className="header__nav-btn header__nav-btn--desktop" onClick={() => navigate('/my-listings')}>
                My Listings
              </button>
              <button className="header__nav-btn header__nav-btn--desktop" onClick={() => navigate('/home')}>
                Browse
              </button>
              <button className="header__nav-btn header__nav-btn--desktop" onClick={handleLogout}>
                Sign Out
              </button>

              {/* Mobile hamburger */}
              <div className="header__mobile-menu" ref={menuRef}>
                <button
                  className="header__hamburger"
                  onClick={() => setMenuOpen(o => !o)}
                  aria-label="Menu"
                >
                  <span /><span /><span />
                </button>
                {menuOpen && (
                  <div className="header__dropdown">
                    <div className="header__dropdown-user">{user?.name}</div>
                    <button className="header__dropdown-item header__dropdown-item--primary" onClick={() => navTo('/create')}>+ List Item</button>
                    <button className="header__dropdown-item" onClick={() => navTo('/home')}>Browse</button>
                    <button className="header__dropdown-item" onClick={() => navTo('/my-listings')}>My Listings</button>
                    <button className="header__dropdown-item header__dropdown-item--danger" onClick={() => { setMenuOpen(false); handleLogout(); }}>Sign Out</button>
                  </div>
                )}
              </div>
            </>
          ) : (
            <span className="header__tagline">UW Students Only</span>
          )}
        </nav>
      </div>
    </header>
  );
}
