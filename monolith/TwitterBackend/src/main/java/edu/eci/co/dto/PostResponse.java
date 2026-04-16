package edu.eci.co.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(name = "PostResponse", description = "Post payload returned by the API")
public class PostResponse {
    @Schema(description = "Post identifier", example = "1")
    private long id;

    @Schema(description = "Post content", example = "Hola, este es mi primer post en el stream!")
    private String content;

    @Schema(description = "Author subject from JWT", example = "google-oauth2|105063856870618633482")
    private String authorId;

    @Schema(description = "Author display name resolved from Auth0 claims", example = "gptazo")
    private String authorName;

    @Schema(description = "Creation timestamp in UTC", example = "2026-04-16T20:10:54.147Z")
    private Instant createdAt;
    public PostResponse(){}

    public PostResponse(long id, String content, String authorId, String authorName, Instant createdAt){
        this.id=id;
        this.content=content;
        this.authorId=authorId;
        this.authorName=authorName;
        this.createdAt=createdAt;
    }

    public String getAuthorName() {
        return authorName;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public long getId() {
        return id;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setId(long id) {
        this.id = id;
    }
}
