package edu.eci.co.controller;

import edu.eci.co.dto.MeResponse;
import edu.eci.co.exception.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Profile", description = "Authenticated profile operations")
public class MeController {
    private static final String NS = "https://twitter-like-app.example.com";

    @GetMapping("/api/me")
    @Operation(
            summary = "Get authenticated profile",
            description = "Returns the authenticated user claims from Auth0 custom namespace.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Profile retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MeResponse.class))
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
    public MeResponse me (@AuthenticationPrincipal Jwt jwt){
        return new MeResponse(
                jwt.getSubject(),
                jwt.getClaimAsString(NS + "/name"),
                jwt.getClaimAsString(NS + "/nickname"),
                jwt.getClaimAsString(NS + "/email")
        );
    }
}
