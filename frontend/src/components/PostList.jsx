export default function PostList({ posts, loading }) {
  if (loading) return <div className="state-box">Cargando posts...</div>;
  if (!posts.length) return <div className="state-box">Todavía no hay publicaciones.</div>;

  return (
    <ul className="post-list">
      {posts.map((post, index) => {
        const key = post.id || post.postId || `${post.content}-${index}`;
        const authorName = typeof post.authorName === 'string' ? post.authorName.trim() : '';
        const author = authorName !== '' ? authorName : 'Anónimo';
        const avatarText = author.charAt(0).toUpperCase();
        const dateValue = post.createdAt || post.timestamp || post.date;
        const dateLabel = dateValue ? new Date(dateValue).toLocaleString() : null;

        return (
          <li key={key} className="post-card">
            <div className="post-head">
              <div className="post-avatar post-avatar-fallback">{avatarText}</div>
              <div className="post-author-wrap">
                <span className="post-author">{author}</span>
                {dateLabel && <span className="post-date">{dateLabel}</span>}
              </div>
            </div>
            <p className="post-content">{post.content || '(sin contenido)'}</p>
          </li>
        );
      })}
    </ul>
  );
}
