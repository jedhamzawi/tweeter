package edu.byu.cs.tweeter.server.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.inject.Guice;
import com.google.inject.Injector;

import edu.byu.cs.tweeter.model.net.request.GetFollowersCountRequest;
import edu.byu.cs.tweeter.model.net.response.GetFollowersCountResponse;
import edu.byu.cs.tweeter.server.dao.dynamo.DynamoModule;
import edu.byu.cs.tweeter.server.service.FollowService;

public class GetFollowersCountHandler implements RequestHandler<GetFollowersCountRequest, GetFollowersCountResponse> {
    @Override
    public GetFollowersCountResponse handleRequest(GetFollowersCountRequest request, Context context) {
        Injector injector = Guice.createInjector(new DynamoModule());
        FollowService followService = injector.getInstance(FollowService.class);
        return followService.getFollowersCount(request);
    }
}
