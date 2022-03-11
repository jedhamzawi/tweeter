package edu.byu.cs.tweeter.server.service;

import edu.byu.cs.tweeter.model.net.request.PostStatusRequest;
import edu.byu.cs.tweeter.model.net.response.PostStatusResponse;

public class StatusService {
    public PostStatusResponse postStatus(PostStatusRequest request) {
        if (request.getStatus() == null) {
            throw new RuntimeException("[BadRequest] Missing a username");
        }
        /*
        FIXME: Null checking is broken using dummy data. Add back in for production
        else if(request.getAuthToken() == null) {
            throw new RuntimeException("[BadRequest] Missing an authToken");
        }
         */

        //TODO: Post status in database
        return new PostStatusResponse(true);
    }
}
