package edu.byu.cs.tweeter.server.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.inject.Guice;
import com.google.inject.Injector;

import edu.byu.cs.tweeter.model.net.request.TestDataRequest;
import edu.byu.cs.tweeter.model.net.response.TestDataResponse;
import edu.byu.cs.tweeter.server.dao.dynamo.DynamoModule;
import edu.byu.cs.tweeter.server.service.FollowService;
import edu.byu.cs.tweeter.server.service.UserService;

public class TestDataHandler implements RequestHandler<TestDataRequest, TestDataResponse> {
    @Override
    public TestDataResponse handleRequest(TestDataRequest input, Context context) {
        Injector injector = Guice.createInjector(new DynamoModule());
        UserService userService = injector.getInstance(UserService.class);
        FollowService followService = injector.getInstance(FollowService.class);

        userService.putTestUsers();
        followService.followTestUser();

        return new TestDataResponse(true);
    }
}
