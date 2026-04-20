package edu.eci.co.users;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import edu.eci.co.shared.auth.ClaimsUtils;
import edu.eci.co.shared.http.HttpResponses;
import edu.eci.co.users.dto.MeResponse;

public class UserHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
        MeResponse response = new MeResponse(
                ClaimsUtils.subject(event),
                ClaimsUtils.name(event),
                ClaimsUtils.nickname(event),
                ClaimsUtils.email(event)
        );
        return HttpResponses.json(200, response);
    }
}
