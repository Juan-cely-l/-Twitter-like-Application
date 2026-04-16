const AUTH0_AUDIENCE = import.meta.env.VITE_AUTH0_AUDIENCE;
const BASE_SCOPE = 'openid profile email';
const API_SCOPE = 'read:profile write:posts';
const LOGIN_SCOPE = `${BASE_SCOPE} ${API_SCOPE}`;

function normalizeScopes(scope) {
  const scopes = `${BASE_SCOPE} ${scope || ''}`.trim().split(/\s+/).filter(Boolean);
  return [...new Set(scopes)].join(' ');
}

function isConsentRequired(error) {
  const code = error?.error || error?.code;
  const message = (error?.message || '').toLowerCase();
  return code === 'consent_required' || message.includes('consent_required') || message.includes('consent required');
}

export function getLoginAuthorizationParams() {
  return {
    ...(AUTH0_AUDIENCE ? { audience: AUTH0_AUDIENCE } : {}),
    scope: LOGIN_SCOPE,
  };
}

export async function getTokenOrRedirect({ getAccessTokenSilently, loginWithRedirect, scope }) {
  try {
    return await getAccessTokenSilently({
      authorizationParams: {
        ...(AUTH0_AUDIENCE ? { audience: AUTH0_AUDIENCE } : {}),
        scope: normalizeScopes(scope),
      },
    });
  } catch (error) {
    if (isConsentRequired(error)) {
      await loginWithRedirect({
        authorizationParams: getLoginAuthorizationParams(),
      });
      return null;
    }
    throw error;
  }
}
