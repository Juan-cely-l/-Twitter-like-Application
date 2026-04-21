import { useState } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import { createPost } from '../services/api';
import { getTokenOrRedirect } from '../utils/auth';

const MAX_CHARS = 140;

export default function PostForm({ isAuthenticated, onPostCreated }) {
  const { getAccessTokenSilently, loginWithRedirect, user } = useAuth0();
  const [content, setContent] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const chars = content.length;
  const isEmpty = content.trim().length === 0;
  const isTooLong = chars > MAX_CHARS;
  const disabled = !isAuthenticated || loading;
  const displayName = user?.name || user?.nickname || user?.email || 'Invitado';
  const avatarText = displayName.charAt(0).toUpperCase();

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError('');
    setSuccess('');

    if (isEmpty) {
      setError('El post no puede estar vacío.');
      return;
    }
    if (isTooLong) {
      setError('El post no puede superar 140 caracteres.');
      return;
    }
    if (!isAuthenticated) {
      setError('Debes iniciar sesión para publicar.');
      return;
    }

    setLoading(true);
    try {
      const token = await getTokenOrRedirect({
        getAccessTokenSilently,
        loginWithRedirect,
        scope: 'write:posts',
      });
      if (!token) return;
      await createPost(token, content.trim(), displayName);
      setContent('');
      setSuccess('Post publicado correctamente.');
      await onPostCreated();
    } catch (error) {
      setError(error.message || 'No se pudo publicar el post.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <section>
      <h2 className="composer-title">Crear post</h2>
      <div className="composer-head">
        {user?.picture ? (
          <img className="composer-avatar" src={user.picture} alt={`Avatar de ${displayName}`} />
        ) : (
          <div className="composer-avatar composer-avatar-fallback">{avatarText}</div>
        )}
        <div>
          <p>{displayName}</p>
          <p className="composer-subtitle">
            {isAuthenticated ? 'Comparte algo con la comunidad' : 'Inicia sesión para publicar'}
          </p>
        </div>
      </div>
      <form onSubmit={handleSubmit}>
        <textarea
          className="composer-input"
          value={content}
          onChange={(event) => setContent(event.target.value)}
          maxLength={MAX_CHARS}
          rows={4}
          placeholder={isAuthenticated ? '¿Qué está pasando?' : 'Inicia sesión para publicar'}
          disabled={disabled}
        />
        <div className="composer-footer">
          <p className={`composer-counter ${chars > 120 ? 'warning' : ''}`}>{chars}/{MAX_CHARS}</p>
          <button className="btn btn-primary" type="submit" disabled={disabled || isEmpty || isTooLong}>
            {loading ? 'Publicando...' : 'Publicar'}
          </button>
        </div>
      </form>
      <div className="form-messages">
        {error && <p className="error-text">{error}</p>}
        {success && <p className="success-text">{success}</p>}
        {!isAuthenticated && <p className="muted-text">Debes iniciar sesión para crear posts.</p>}
      </div>
    </section>
  );
}
