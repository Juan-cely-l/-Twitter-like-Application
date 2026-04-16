import { useEffect, useState } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import { getPublicPosts } from './services/api';
import './App.css';
import LoginButton from './components/LoginButton';
import LogoutButton from './components/LogoutButton';
import UserProfile from './components/UserProfile';
import PostForm from './components/PostForm';
import PostList from './components/PostList';
import MePanel from './components/MePanel';

export default function App() {
  const { isLoading: authLoading, error: authError, isAuthenticated } = useAuth0();
  const [posts, setPosts] = useState([]);
  const [postsLoading, setPostsLoading] = useState(false);
  const [postsError, setPostsError] = useState('');

  const loadPosts = async () => {
    setPostsLoading(true);
    setPostsError('');
    try {
      const data = await getPublicPosts();
      setPosts(Array.isArray(data) ? data : []);
    } catch (error) {
      setPostsError(error.message || 'No se pudieron cargar los posts');
    } finally {
      setPostsLoading(false);
    }
  };

  useEffect(() => {
    loadPosts();
  }, []);

  if (authLoading) {
    return (
      <main className="app-shell">
        <div className="state-box">Cargando autenticación...</div>
      </main>
    );
  }

  return (
    <main className="app-shell">
      <div className="app-grid">
        <aside className="column column-left">
          <section className="card brand-card">
            <div className="brand-mark">TL</div>
            <div>
              <p className="kicker">Microblog</p>
              <h1 className="brand-title">Twitter-like App</h1>
            </div>
          </section>

          <section className="card">
            <h2 className="section-title">Cuenta</h2>
            <p className="muted-text">
              {isAuthenticated ? 'Sesión activa' : 'Sesión cerrada'}
            </p>
            <div className="stack-sm">
              {isAuthenticated ? <LogoutButton /> : <LoginButton />}
              {authError && <p className="error-text">Error Auth0: {authError.message}</p>}
            </div>
          </section>

          <section className="card">
            <h2 className="section-title">Actividad</h2>
            <div className="stat-row">
              <span className="muted-text">Posts cargados</span>
              <strong>{posts.length}</strong>
            </div>
            <div className="stat-row">
              <span className="muted-text">Estado</span>
              <strong>{postsLoading ? 'Actualizando' : 'Al día'}</strong>
            </div>
          </section>
        </aside>

        <section className="column column-feed">
          <header className="card feed-header-card">
            <div>
              <p className="kicker">Inicio</p>
              <h2 className="feed-main-title">Feed público</h2>
            </div>
            <button className="btn btn-secondary" type="button" onClick={loadPosts} disabled={postsLoading}>
              {postsLoading ? 'Cargando...' : 'Refrescar'}
            </button>
          </header>

          <section className="card">
            <PostForm isAuthenticated={isAuthenticated} onPostCreated={loadPosts} />
          </section>

          <section className="card">
            {postsError && <p className="error-text">{postsError}</p>}
            <PostList posts={posts} loading={postsLoading} />
          </section>
        </section>

        <aside className="column column-right">
          <section className="card">
            <UserProfile />
          </section>

          <section className="card">
            <MePanel />
          </section>
        </aside>
      </div>
    </main>
  );
}
