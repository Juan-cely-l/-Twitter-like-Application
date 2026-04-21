const API_BASE_URL = 'http://localhost:8080';

async function request(path, options = {}) {
  const response = await fetch(`${API_BASE_URL}${path}`, options);

  if (!response.ok) {
    const message = await response.text();
    throw new Error(message || `HTTP ${response.status}`);
  }

  if (response.status === 204) return null;
  return response.json();
}

export function getPublicPosts() {
  return request('/api/posts');
}

export function getMe(accessToken) {
  return request('/api/me', {
    headers: {
      Authorization: `Bearer ${accessToken}`,
    },
  });
}

export function createPost(accessToken, content, authorName) {
  return request('/api/posts', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${accessToken}`,
    },
    body: JSON.stringify({ content, authorName }),
  });
}
