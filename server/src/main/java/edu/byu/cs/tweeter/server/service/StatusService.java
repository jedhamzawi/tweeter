package edu.byu.cs.tweeter.server.service;

import javax.inject.Inject;

import edu.byu.cs.tweeter.model.net.request.PostStatusRequest;
import edu.byu.cs.tweeter.model.net.response.PostStatusResponse;
import edu.byu.cs.tweeter.server.dao.StatusDAO;
import edu.byu.cs.tweeter.server.dao.dynamo.StatusDynamoDAO;

public class StatusService {
    private final StatusDAO statusDAO;

    @Inject
    public StatusService(StatusDAO statusDAO) {
        this.statusDAO = statusDAO;
    }

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

        return getStatusDAO().postStatus(request);
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
