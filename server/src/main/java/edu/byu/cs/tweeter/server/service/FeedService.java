package edu.byu.cs.tweeter.server.service;

import com.google.inject.Inject;

import edu.byu.cs.tweeter.model.net.request.GetFeedRequest;
import edu.byu.cs.tweeter.model.net.response.GetFeedResponse;
import edu.byu.cs.tweeter.server.dao.StatusDAO;
import edu.byu.cs.tweeter.server.dao.dynamo.StatusDynamoDAO;

public class FeedService {
    private final StatusDAO statusDAO;

    @Inject
    public FeedService(StatusDAO statusDAO) {
        this.statusDAO = statusDAO;
    }

    public GetFeedResponse getFeed(GetFeedRequest request) {
        if(request.getTargetUser() == null || request.getTargetUser().alias == null) {
            throw new RuntimeException("[BadRequest] Request needs to have a follower alias");
        } else if(request.getLimit() <= 0) {
            throw new RuntimeException("[BadRequest] Request needs to have a positive limit");
        }

        return getStatusDAO().getFeed(request);
    }

    /**
     * Returns an instance of {@link StatusDynamoDAO}. Allows mocking of the StatusDAO class
     * for testing purposes. All usages of StatusDAO should get their StatusDAO
     * instance from this method to allow for mocking of the instance.
     *
     * @return the instance.
     */
    public StatusDAO getStatusDAO() { return this.statusDAO; }
}
