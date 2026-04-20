package edu.eci.co.shared.http;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import edu.eci.co.shared.dto.ApiError;
import edu.eci.co.shared.json.JsonUtils;

import java.util.Map;

public final class HttpResponses {
    private static final Map<String, String> JSON_HEADERS = Map.of("Content-Type", "application/json");

    private HttpResponses() {
    }

    public static APIGatewayV2HTTPResponse json(int statusCode, Object body) {
        try {
            return APIGatewayV2HTTPResponse.builder()
                    .withStatusCode(statusCode)
                    .withHeaders(JSON_HEADERS)
                    .withBody(JsonUtils.toJson(body))
                    .build();
        } catch (Exception ex) {
            return APIGatewayV2HTTPResponse.builder()
                    .withStatusCode(500)
                    .withHeaders(JSON_HEADERS)
                    .withBody("{\"message\":\"Internal server error\"}")
                    .build();
        }
    }

    public static APIGatewayV2HTTPResponse error(int statusCode, String error, String message, String path) {
        return json(statusCode, ApiError.of(statusCode, error, message, path));
    }
}
