package edu.eci.co.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ApiError", description = "Standard API error response")
public record ApiError(
        @Schema(description = "Error timestamp in UTC", example = "2026-04-16T20:10:54.147Z")
        Instant timestamp,
        @Schema(description = "HTTP status code", example = "400")
        int status,
        @Schema(description = "HTTP reason phrase", example = "Bad Request")
        String error,
        @Schema(description = "Human-readable error message", example = "Validation failed")
        String message,
        @Schema(description = "Request path", example = "/api/posts")
        String path,
        @Schema(description = "Validation details by field when status is 400")
        Map<String, String> validationErrors
) {
    public static ApiError of(HttpStatus status, String message, String path) {
        return new ApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path,
                null
        );
    }

    public static ApiError validation(String message, String path, Map<String, String> validationErrors) {
        return new ApiError(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                message,
                path,
                validationErrors
        );
    }
}
