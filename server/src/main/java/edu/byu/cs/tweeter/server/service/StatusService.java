package edu.byu.cs.tweeter.server.service;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import edu.byu.cs.tweeter.model.net.request.PostStatusRequest;
import edu.byu.cs.tweeter.model.net.response.PostStatusResponse;
import edu.byu.cs.tweeter.server.dao.FollowDAO;
import edu.byu.cs.tweeter.server.dao.StatusDAO;
import edu.byu.cs.tweeter.server.dao.UserDAO;
import edu.byu.cs.tweeter.server.dao.dynamo.StatusDynamoDAO;

public class StatusService extends Service {
    private final StatusDAO statusDAO;
    private final UserDAO userDAO;
    private final FollowDAO followDAO;

    @Inject
    public StatusService(StatusDAO statusDAO, UserDAO userDAO, FollowDAO followDAO) {
        this.statusDAO = statusDAO;
        this.userDAO = userDAO;
        this.followDAO = followDAO;
    }

    /**
     * Returns an instance of {@link StatusDynamoDAO}. Allows mocking of the StatusDAO class
     * for testing purposes. All usages of StatusDAO should get their StatusDAO
     * instance from this method to allow for mocking of the instance.
     *
     * @return the instance.
     */
    public StatusDAO getStatusDAO() { return this.statusDAO; }

    @Override
    public UserDAO getUserDAO() { return this.userDAO; }

    public FollowDAO getFollowDAO() { return this.followDAO; }

    public PostStatusResponse postStatus(PostStatusRequest request) {
        if (request.getStatus() == null) {
            throw new RuntimeException("[BadRequest] Request requires a status");
        }
        if (request.getStatus().getUser() == null || request.getStatus().getUser().getAlias() == null) {
            throw new RuntimeException("[BadRequest] Request requires a poster");
        }
        else if(request.getAuthToken() == null) {
            throw new RuntimeException("[BadRequest] Request requires an authToken");
        }

        if (!authenticate(request.getAuthToken())) {
            return new PostStatusResponse("Unable to authenticate! Your session may have expired. Please log out and log back in.");
        }

        request.getStatus().setID(generateDatetime() + " : " + UUID.randomUUID());
        String mentionsString = deserializeList(request.getStatus().getMentions());
        String urlsString = deserializeList(request.getStatus().getUrls());
        try {
            getStatusDAO().postStatusToStory(
                    request.getStatus().getUser().getAlias(),
                    request.getStatus().getPost(),
                    mentionsString,
                    urlsString,
                    request.getStatus().getDatetime(),
                    request.getStatus().getID()
            );
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("[DBError] Unable to post status to story: " + e.getMessage());
        }

        try {
            List<String> followers = getFollowDAO().getFollowers(request.getStatus().getUser().getAlias(), 25, null);
            System.out.printf("Size of followers: %d%n", followers.size());
            while (followers != null && !followers.isEmpty()) {
                getStatusDAO().postStatusToFeeds(request.getStatus().getID(), followers, request.getStatus().getUser().getAlias());
                followers = getFollowDAO().getFollowers(request.getStatus().getUser().getAlias(), 25, followers.get(followers.size() - 1));
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("[DBError] Unable to update followers' feeds: " + e.getMessage());
        }

        return new PostStatusResponse(true);
    }
}
