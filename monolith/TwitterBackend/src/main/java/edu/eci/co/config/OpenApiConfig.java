package edu.eci.co.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Twitter Like Monolith API",
                version = "v1",
                description = "Spring Boot monolith secured with Auth0 JWT for posts and profile endpoints."
        ),
        tags = {
                @Tag(name = "Posts", description = "Public feed and post creation operations"),
                @Tag(name = "Profile", description = "Authenticated profile operations")
        }
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Auth0 access token. Required scopes: write:posts for POST /api/posts and read:profile for GET /api/me."
)
public class OpenApiConfig {
}
