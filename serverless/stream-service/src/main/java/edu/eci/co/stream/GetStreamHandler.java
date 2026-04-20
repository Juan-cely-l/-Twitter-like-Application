package edu.eci.co.stream;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import edu.eci.co.shared.dto.PostResponse;
import edu.eci.co.shared.http.HttpResponses;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;

import java.util.List;
import java.util.Map;

public class GetStreamHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {
    private static final String STREAM_ID = "global";
    private static final int DEFAULT_LIMIT = 100;

    private final DynamoDbClient dynamoDb;
    private final String tableName;

    public GetStreamHandler() {
        this(DynamoDbClient.create(), System.getenv("POSTS_TABLE_NAME"));
    }

    GetStreamHandler(DynamoDbClient dynamoDb, String tableName) {
        this.dynamoDb = dynamoDb;
        this.tableName = tableName;
    }

    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
        if (tableName == null || tableName.isBlank()) {
            return HttpResponses.error(500, "Internal Server Error", "POSTS_TABLE_NAME is not configured", path(event));
        }

        try {
            QueryRequest query = QueryRequest.builder()
                    .tableName(tableName)
                    .keyConditionExpression("streamId = :streamId")
                    .expressionAttributeValues(Map.of(":streamId", s(STREAM_ID)))
                    .scanIndexForward(false)
                    .limit(DEFAULT_LIMIT)
                    .build();

            List<PostResponse> posts = dynamoDb.query(query)
                    .items()
                    .stream()
                    .map(this::toPostResponse)
                    .toList();

            return HttpResponses.json(200, posts);
        } catch (Exception ex) {
            context.getLogger().log("Error reading stream: " + ex.getMessage());
            return HttpResponses.error(500, "Internal Server Error", "Could not load stream", path(event));
        }
    }

    private PostResponse toPostResponse(Map<String, AttributeValue> item) {
        return new PostResponse(
                value(item, "postId"),
                value(item, "content"),
                value(item, "authorId"),
                value(item, "authorName"),
                value(item, "createdAt")
        );
    }

    private String value(Map<String, AttributeValue> item, String key) {
        AttributeValue value = item.get(key);
        return value == null || value.s() == null ? "" : value.s();
    }

    private AttributeValue s(String value) {
        return AttributeValue.builder().s(value).build();
    }

    private String path(APIGatewayV2HTTPEvent event) {
        return event == null || event.getRawPath() == null ? "/api/stream" : event.getRawPath();
    }
}
