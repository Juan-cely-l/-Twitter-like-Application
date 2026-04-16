import { useAuth0 } from '@auth0/auth0-react';
import { getLoginAuthorizationParams } from '../utils/auth';

export default function LoginButton() {
  const { loginWithRedirect } = useAuth0();

  return (
    <button
      className="btn btn-primary btn-full"
      type="button"
      onClick={() => loginWithRedirect({ authorizationParams: getLoginAuthorizationParams() })}
    >
      Iniciar sesión
    </button>
  );
}
