package edu.eci.co.posts.model;

public record PostItem(
        String streamId,
        String postKey,
        String postId,
        String content,
        String authorId,
        String authorName,
        String createdAt
) {
}
