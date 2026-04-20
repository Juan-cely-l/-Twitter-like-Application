package edu.eci.co.shared.dto;

public record PostResponse(
        String id,
        String content,
        String authorId,
        String authorName,
        String createdAt
) {
}
