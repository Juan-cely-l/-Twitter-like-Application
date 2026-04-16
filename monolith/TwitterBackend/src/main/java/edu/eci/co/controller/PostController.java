package edu.eci.co.controller;

import edu.eci.co.dto.CreatePostRequest;
import edu.eci.co.dto.PostResponse;
import edu.eci.co.exception.ApiError;
import edu.eci.co.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api")
@Tag(name = "Posts", description = "Public feed and post creation operations")
public class PostController {
    private static final String NS = "https://twitter-like-app.example.com";
    private final PostService postService;
    public PostController(PostService postService){
        this.postService=postService;
    }

    @GetMapping("/posts")
    @Operation(
            summary = "Get public posts",
            description = "Returns all posts ordered by creation date descending. Public endpoint."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Posts retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = PostResponse.class))
                    )
            )
    })
    public List<PostResponse>getPosts(){
        return postService.getAllPosts();
    }

    @GetMapping("/stream")
    @Operation(
            summary = "Get public stream",
            description = "Returns the public stream feed. Public endpoint."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Stream retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = PostResponse.class))
                    )
            )
    })
    public ResponseEntity<List<PostResponse>> getStream() {
        return ResponseEntity.ok(postService.getAllPosts());
    }

    @PostMapping("/posts")
    @Operation(
            summary = "Create a new post",
            description = "Creates a post for the authenticated user. Requires Auth0 JWT and scope write:posts.",
            security = @SecurityRequirement(name = "bearerAuth"),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Post payload",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CreatePostRequest.class)
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Post created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PostResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Insufficient scope",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            )
    })
    public PostResponse createPost(
            @Valid @RequestBody CreatePostRequest request,
            @AuthenticationPrincipal Jwt jwt
            ){
        String authorId= jwt.getSubject();
        String authorName = firstNonBlank(
                jwt.getClaimAsString(NS + "/name"),
                jwt.getClaimAsString(NS + "/nickname"),
                jwt.getClaimAsString(NS + "/email"),
                jwt.getSubject()
        );

        return postService.createPost(request,authorId,authorName);
    }
    private String firstNonBlank(String ...values ){
        for (String value: values){
            if(value!=null && !value.isBlank()){
                return value;}
        }
        return "unknown";
    }

}
