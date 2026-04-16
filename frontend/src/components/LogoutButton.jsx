import { useAuth0 } from '@auth0/auth0-react';

export default function LogoutButton() {
  const { logout } = useAuth0();

  return (
    <button
      className="btn btn-danger btn-full"
      type="button"
      onClick={() =>
        logout({
          logoutParams: {
            returnTo: window.location.origin,
          },
        })
      }
    >
      Cerrar sesión
    </button>
  );
}
