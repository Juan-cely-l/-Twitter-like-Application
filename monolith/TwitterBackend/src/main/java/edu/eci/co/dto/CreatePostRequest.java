package edu.eci.co.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


@Schema(name = "CreatePostRequest", description = "Request payload to create a post")
public class CreatePostRequest {
    @NotBlank
    @Size(max = 140)
    @Schema(
            description = "Post content. Must not be blank and must be at most 140 characters.",
            example = "Hola, este es mi primer post en el stream!",
            maxLength = 140,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String content;

        @Size(max = 120)
        @Schema(
            description = "Optional display name sent by the frontend. Used only when profile claims are not present in the access token.",
            example = "Roger Rodriguez",
            maxLength = 120,
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        private String authorName;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }
}
