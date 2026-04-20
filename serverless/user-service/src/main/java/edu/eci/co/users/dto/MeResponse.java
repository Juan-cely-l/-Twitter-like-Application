package edu.eci.co.users.dto;

public record MeResponse(
        String sub,
        String name,
        String nickname,
        String email
) {
}
