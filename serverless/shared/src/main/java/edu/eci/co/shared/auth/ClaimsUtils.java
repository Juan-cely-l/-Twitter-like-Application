package edu.eci.co.shared.auth;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;

import java.util.Map;

public final class ClaimsUtils {
    private static final String DEFAULT_CLAIMS_NAMESPACE = "https://twitter-like-app.example.com";

    private ClaimsUtils() {
    }

    public static Map<String, String> claims(APIGatewayV2HTTPEvent event) {
        if (event == null || event.getRequestContext() == null
                || event.getRequestContext().getAuthorizer() == null
                || event.getRequestContext().getAuthorizer().getJwt() == null
                || event.getRequestContext().getAuthorizer().getJwt().getClaims() == null) {
            return Map.of();
        }
        return event.getRequestContext().getAuthorizer().getJwt().getClaims();
    }

    public static String subject(APIGatewayV2HTTPEvent event) {
        return claim(event, "sub");
    }

    public static String name(APIGatewayV2HTTPEvent event) {
        return firstNonBlank(namespacedClaim(event, "name"), claim(event, "name"));
    }

    public static String nickname(APIGatewayV2HTTPEvent event) {
        return firstNonBlank(namespacedClaim(event, "nickname"), claim(event, "nickname"));
    }

    public static String email(APIGatewayV2HTTPEvent event) {
        return firstNonBlank(namespacedClaim(event, "email"), claim(event, "email"));
    }

    public static String displayName(APIGatewayV2HTTPEvent event) {
        return firstNonBlank(name(event), nickname(event), email(event), subject(event), "unknown");
    }

    public static String claim(APIGatewayV2HTTPEvent event, String key) {
        String value = claims(event).get(key);
        return value == null ? "" : value;
    }

    private static String namespacedClaim(APIGatewayV2HTTPEvent event, String key) {
        return claim(event, claimsNamespace() + "/" + key);
    }

    private static String claimsNamespace() {
        String namespace = System.getenv("AUTH0_CLAIMS_NAMESPACE");
        if (namespace == null || namespace.isBlank()) {
            return DEFAULT_CLAIMS_NAMESPACE;
        }
        return namespace.endsWith("/") ? namespace.substring(0, namespace.length() - 1) : namespace;
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }
}
