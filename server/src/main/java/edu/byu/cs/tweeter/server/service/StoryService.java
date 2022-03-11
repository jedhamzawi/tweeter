package edu.byu.cs.tweeter.server.service;

import edu.byu.cs.tweeter.model.net.request.GetStoryRequest;
import edu.byu.cs.tweeter.model.net.response.GetStoryResponse;
import edu.byu.cs.tweeter.server.dao.StatusDAO;

public class StoryService {

    public GetStoryResponse getStory(GetStoryRequest request) {
        if(request.getTargetUser() == null || request.getTargetUser().alias == null) {
            throw new RuntimeException("[BadRequest] Request needs to have a follower alias");
        } else if(request.getLimit() <= 0) {
            throw new RuntimeException("[BadRequest] Request needs to have a positive limit");
        }

        return getStatusDAO().getStory(request);
    }

    /**
     * Returns an instance of {@link StatusDAO}. Allows mocking of the StatusDAO class
     * for testing purposes. All usages of StatusDAO should get their StatusDAO
     * instance from this method to allow for mocking of the instance.
     *
     * @return the instance.
     */
    public StatusDAO getStatusDAO() { return new StatusDAO(); }
}
