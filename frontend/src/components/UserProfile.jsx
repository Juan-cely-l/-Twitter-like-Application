import { useAuth0 } from '@auth0/auth0-react';

export default function UserProfile() {
  const { isAuthenticated, user } = useAuth0();

  if (!isAuthenticated) {
    return (
      <section>
        <h2 className="section-title">Perfil</h2>
        <p className="muted-text">Inicia sesión para ver tus datos de cuenta.</p>
      </section>
    );
  }

  const displayName = user?.name || user?.nickname || user?.email || 'Usuario';
  const handle = user?.nickname || user?.email || 'usuario';
  const avatarText = displayName.charAt(0).toUpperCase();
  const picture = user?.picture;

  return (
    <section>
      <h2 className="section-title">Perfil autenticado</h2>
      <div className="profile-card-head">
        {picture ? (
          <img className="profile-avatar" src={picture} alt={`Avatar de ${displayName}`} />
        ) : (
          <div className="profile-avatar profile-avatar-fallback">{avatarText}</div>
        )}
        <div>
          <p className="profile-name">{displayName}</p>
          <p className="profile-handle">@{handle}</p>
        </div>
      </div>
      <details className="profile-details">
        <summary>Ver perfil completo (JSON)</summary>
        <pre className="json-view">{JSON.stringify(user, null, 2)}</pre>
      </details>
    </section>
  );
}
