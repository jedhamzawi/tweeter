package edu.byu.cs.tweeter.server.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.inject.Guice;
import com.google.inject.Injector;

import edu.byu.cs.tweeter.model.net.request.GetFollowingCountRequest;
import edu.byu.cs.tweeter.model.net.response.GetFollowingCountResponse;
import edu.byu.cs.tweeter.server.dao.dynamo.DynamoModule;
import edu.byu.cs.tweeter.server.service.FollowService;

public class GetFollowingCountHandler implements RequestHandler<GetFollowingCountRequest, GetFollowingCountResponse> {
    @Override
    public GetFollowingCountResponse handleRequest(GetFollowingCountRequest request, Context context) {
        Injector injector = Guice.createInjector(new DynamoModule());
        FollowService followService = injector.getInstance(FollowService.class);
        return followService.getFollowingCount(request);
    }
}
