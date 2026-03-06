import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { GoogleLogin } from '@react-oauth/google';
import axios from 'axios';
import { useAuth } from '../context/AuthContext';
import './Login.css';

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

export default function Login() {
  const { login, isAuthenticated } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (isAuthenticated) navigate('/home', { replace: true });
  }, [isAuthenticated, navigate]);

  async function handleGoogleSuccess(credentialResponse) {
    try {
      const res = await axios.post(`${API_URL}/api/auth/google`, {
        idToken: credentialResponse.credential,
      });
      login(res.data.token, res.data.user);
      navigate('/home', { replace: true });
    } catch (err) {
      const msg =
        err.response?.data?.message || 'Login failed. Only @uw.edu accounts are allowed.';
      alert(msg);
    }
  }

  return (
    <div className="login-page">
      <div className="login-content">
        <p className="login-eyebrow animate-el delay-0">Dawgs Marketplace</p>
        <h1 className="login-title animate-el delay-1">Welcome</h1>
        <p className="login-desc animate-el delay-2">
          Access your account and browse the marketplace
        </p>

        <div className="login-google-wrap animate-el delay-3">
          <GoogleLogin
            onSuccess={handleGoogleSuccess}
            onError={() => alert('Google login failed. Please try again.')}
            useOneTap={false}
            shape="rectangular"
            theme="outline"
            size="large"
            text="continue_with"
            logo_alignment="left"
            width="320"
          />
        </div>

        <p className="login-note animate-el delay-4">
          Restricted to <strong>@uw.edu</strong> accounts only
        </p>
      </div>
    </div>
  );
}
