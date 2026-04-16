import { useState } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import { getMe } from '../services/api';
import { getTokenOrRedirect } from '../utils/auth';

export default function MePanel() {
  const { isAuthenticated, getAccessTokenSilently, loginWithRedirect } = useAuth0();
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const loadMe = async () => {
    if (!isAuthenticated) return;

    setLoading(true);
    setError('');
    try {
      const token = await getTokenOrRedirect({
        getAccessTokenSilently,
        loginWithRedirect,
        scope: 'read:profile',
      });
      if (!token) return;
      const response = await getMe(token);
      setData(response);
    } catch (error) {
      setError(error.message || 'No se pudo consultar /api/me');
    } finally {
      setLoading(false);
    }
  };

  return (
    <section>
      <div className="me-panel-top">
        <h2 className="section-title">Panel /api/me</h2>
        <button className="btn btn-secondary" type="button" onClick={loadMe} disabled={!isAuthenticated || loading}>
          {loading ? 'Consultando...' : 'Consultar'}
        </button>
      </div>
      {!isAuthenticated && <p className="muted-text">Disponible al iniciar sesión.</p>}
      {error && <p className="error-text">{error}</p>}
      {data && <pre className="json-view">{JSON.stringify(data, null, 2)}</pre>}
    </section>
  );
}
