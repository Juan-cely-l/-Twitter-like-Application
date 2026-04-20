package edu.eci.co.posts;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import edu.eci.co.posts.dto.CreatePostRequest;
import edu.eci.co.posts.model.PostItem;
import edu.eci.co.shared.auth.ClaimsUtils;
import edu.eci.co.shared.dto.ApiError;
import edu.eci.co.shared.dto.PostResponse;
import edu.eci.co.shared.http.HttpResponses;
import edu.eci.co.shared.json.JsonUtils;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class CreatePostHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {
    private static final int MAX_CONTENT_LENGTH = 140;
    private static final String STREAM_ID = "global";

    private final DynamoDbClient dynamoDb;
    private final String tableName;

    public CreatePostHandler() {
        this(DynamoDbClient.create(), System.getenv("POSTS_TABLE_NAME"));
    }

    CreatePostHandler(DynamoDbClient dynamoDb, String tableName) {
        this.dynamoDb = dynamoDb;
        this.tableName = tableName;
    }

    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
        if (tableName == null || tableName.isBlank()) {
            return HttpResponses.error(500, "Internal Server Error", "POSTS_TABLE_NAME is not configured", path(event));
        }

        String body = event == null ? null : event.getBody();
        if (body == null || body.isBlank()) {
            return HttpResponses.error(400, "Bad Request", "Request body is required", path(event));
        }

        CreatePostRequest request;
        try {
            request = JsonUtils.fromJson(body, CreatePostRequest.class);
        } catch (JsonProcessingException ex) {
            return HttpResponses.error(400, "Bad Request", "Request body must be valid JSON", path(event));
        }

        Map<String, String> validationErrors = validate(request);
        if (!validationErrors.isEmpty()) {
            return HttpResponses.json(400, ApiError.validation("Validation failed", path(event), validationErrors));
        }

        try {
            String postId = UUID.randomUUID().toString();
            String createdAt = Instant.now().toString();
            String content = request.content().trim();
            String authorId = ClaimsUtils.subject(event);
            String authorName = ClaimsUtils.displayName(event);

            PostItem item = new PostItem(
                    STREAM_ID,
                    createdAt + "#" + postId,
                    postId,
                    content,
                    authorId,
                    authorName,
                    createdAt
            );

            dynamoDb.putItem(PutItemRequest.builder()
                    .tableName(tableName)
                    .item(toDynamoItem(item))
                    .build());

            return HttpResponses.json(200, new PostResponse(
                    item.postId(),
                    item.content(),
                    item.authorId(),
                    item.authorName(),
                    item.createdAt()
            ));
        } catch (Exception ex) {
            context.getLogger().log("Error creating post: " + ex.getMessage());
            return HttpResponses.error(500, "Internal Server Error", "Could not create post", path(event));
        }
    }

    private Map<String, String> validate(CreatePostRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        String content = request == null ? null : request.content();

        if (content == null || content.isBlank()) {
            errors.put("content", "must not be blank");
            return errors;
        }

        if (content.trim().length() > MAX_CONTENT_LENGTH) {
            errors.put("content", "size must be between 1 and 140");
        }

        return errors;
    }

    private Map<String, AttributeValue> toDynamoItem(PostItem item) {
        return Map.of(
                "streamId", s(item.streamId()),
                "postKey", s(item.postKey()),
                "postId", s(item.postId()),
                "content", s(item.content()),
                "authorId", s(item.authorId()),
                "authorName", s(item.authorName()),
                "createdAt", s(item.createdAt())
        );
    }

    private AttributeValue s(String value) {
        return AttributeValue.builder().s(value).build();
    }

    private String path(APIGatewayV2HTTPEvent event) {
        return event == null || event.getRawPath() == null ? "/api/posts" : event.getRawPath();
    }
}
