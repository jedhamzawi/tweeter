package edu.byu.cs.tweeter.server.service;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import edu.byu.cs.tweeter.model.net.request.FollowRequest;
import edu.byu.cs.tweeter.model.net.request.GetFollowersCountRequest;
import edu.byu.cs.tweeter.model.net.request.GetFollowersRequest;
import edu.byu.cs.tweeter.model.net.request.GetFollowingCountRequest;
import edu.byu.cs.tweeter.model.net.request.GetFollowingRequest;
import edu.byu.cs.tweeter.model.net.request.IsFollowerRequest;
import edu.byu.cs.tweeter.model.net.request.UnfollowRequest;
import edu.byu.cs.tweeter.model.net.response.FollowResponse;
import edu.byu.cs.tweeter.model.net.response.GetFollowersCountResponse;
import edu.byu.cs.tweeter.model.net.response.GetFollowersResponse;
import edu.byu.cs.tweeter.model.net.response.GetFollowingCountResponse;
import edu.byu.cs.tweeter.model.net.response.GetFollowingResponse;
import edu.byu.cs.tweeter.model.net.response.IsFollowerResponse;
import edu.byu.cs.tweeter.model.net.response.UnfollowResponse;
import edu.byu.cs.tweeter.server.dao.DAOException;
import edu.byu.cs.tweeter.server.dao.FollowDAO;
import edu.byu.cs.tweeter.server.dao.UserDAO;
import edu.byu.cs.tweeter.server.dao.dynamo.FollowDynamoDAO;

/**
 * Contains the business logic for getting the users a user is following.
 */
public class FollowService {
    private final FollowDAO followDAO;
    private final UserDAO userDAO;

    @Inject
    public FollowService(FollowDAO followDAO, UserDAO userDAO) {
        this.followDAO = followDAO;
        this.userDAO = userDAO;
    }

    /**
     * Returns an instance of {@link FollowDynamoDAO}. Allows mocking of the FollowDAO class
     * for testing purposes. All usages of FollowDAO should get their FollowDAO
     * instance from this method to allow for mocking of the instance.
     *
     * @return the instance.
     */
    public FollowDAO getFollowDAO() {
        return this.followDAO;
    }

    public UserDAO getUserDAO() { return this.userDAO; }

    /**
     * Returns the users that are following the user that is specified. Uses information in
     * the request object to limit the number of followers returned and to return the next set of
     * followers after any that were returned in a previous request. Uses the {@link FollowDynamoDAO} to
     * get the followers.
     *
     * @param request contains the data required to fulfill the request.
     * @return the followers.
     */
    public GetFollowersResponse getFollowers(GetFollowersRequest request) {
        if (request.getTargetUser() == null || request.getTargetUser().getAlias() == null) {
            throw new RuntimeException("[BadRequest] Request needs to have a follower alias");
        } else if (request.getLimit() <= 0) {
            throw new RuntimeException("[BadRequest] Request needs to have a positive limit");
        }

        List<String> followerAliases;
        try {
            followerAliases = getFollowDAO().getFollowers(request.getTargetUser().getAlias(),
                    request.getLimit(), request.getLastItem() != null ? request.getLastItem().getAlias() : null);
        } catch (DAOException e) {
            throw new RuntimeException("[DBError] Unable to get list of followers: " + e.getMessage());
        }

        if (followerAliases == null) {
            return new GetFollowersResponse(new ArrayList<>(), false);
        }

        try {
            return new GetFollowersResponse(getUserDAO().batchGetUsers(followerAliases),
                    followerAliases.size() == request.getLimit());
        } catch (DAOException e) {
            throw new RuntimeException("[DBError] Unable to get users based on followers: " + e.getMessage());
        }
    }

    /**
     * Returns the users that the user specified in the request is following. Uses information in
     * the request object to limit the number of followees returned and to return the next set of
     * followees after any that were returned in a previous request. Uses the {@link FollowDynamoDAO} to
     * get the followees.
     *
     * @param request contains the data required to fulfill the request.
     * @return the followees.
     */
    public GetFollowingResponse getFollowees(GetFollowingRequest request) {
        if (request.getTargetUser() == null || request.getTargetUser().getAlias() == null) {
            throw new RuntimeException("[BadRequest] Request needs to have a follower alias");
        } else if (request.getLimit() <= 0) {
            throw new RuntimeException("[BadRequest] Request needs to have a positive limit");
        }

        List<String> followeeAliases;
        try {
            followeeAliases = getFollowDAO().getFollowees(request.getTargetUser().getAlias(),
                    request.getLimit(), request.getLastItem() != null ? request.getLastItem().getAlias() : null);
        } catch (DAOException e) {
            throw new RuntimeException("[DBError] Unable to get list of followees: " + e.getMessage());
        }

        if (followeeAliases == null) {
            return new GetFollowingResponse(new ArrayList<>(), false);
        }

        try {
            return new GetFollowingResponse(getUserDAO().batchGetUsers(followeeAliases),
                    followeeAliases.size() == request.getLimit());
        } catch (DAOException e) {
            throw new RuntimeException("[DBError] Unable to get users based on followees: " + e.getMessage());
        }
    }

    public FollowResponse follow(FollowRequest request) {
        if (request.getLoggedInUser() == null || request.getLoggedInUser().getAlias() == null) {
            throw new RuntimeException("[BadRequest] Request needs to have a logged in user");
        }
        if (request.getFollowee() == null || request.getFollowee().getAlias() == null) {
            throw new RuntimeException("[BadRequest] Request needs to have a followee");
        }
        else if (request.getAuthToken() == null) {
            throw new RuntimeException("[BadRequest] Request needs to have an authToken");
        }

        try {
            getFollowDAO().putFollower(request.getFollowee().getAlias(), request.getLoggedInUser().getAlias());
        } catch (DAOException e) {
            throw new RuntimeException("[DBError] Unable to add follower: " + e.getMessage());
        }

        try {
            getUserDAO().incrementFollowingCount(request.getLoggedInUser().getAlias(), 1);
        } catch (DAOException e) {
            throw new RuntimeException("[DBError] Unable to update following count: " + e.getMessage());
        }

        try {
            getUserDAO().incrementFollowerCount(request.getFollowee().getAlias(), 1);
        } catch (DAOException e) {
            throw new RuntimeException("[DBError] Unable to update followers count for user \"" +
                request.getFollowee().getAlias() + "\": " + e.getMessage());
        }

        return new FollowResponse(true);
    }

    public UnfollowResponse unfollow(UnfollowRequest request) {
        if (request.getLoggedInUser() == null || request.getLoggedInUser().getAlias() == null) {
            throw new RuntimeException("[BadRequest] Request needs to have a logged in user");
        }
        if (request.getUnfollowee() == null || request.getUnfollowee().getAlias() == null) {
            throw new RuntimeException("[BadRequest] Request needs to have an unfollowee");
        }
        else if (request.getAuthToken() == null) {
            throw new RuntimeException("[BadRequest] Request needs to have an authToken");
        }

        try {
            getFollowDAO().deleteFollower(request.getUnfollowee().getAlias(), request.getLoggedInUser().getAlias());
        } catch (DAOException e) {
            throw new RuntimeException("[DBError] Unable to delete follower: " + e.getMessage());
        }

        try {
            getUserDAO().incrementFollowingCount(request.getLoggedInUser().getAlias(), -1);
        } catch (DAOException e) {
            throw new RuntimeException("[DBError] Unable to update following count: " + e.getMessage());
        }

        try {
            getUserDAO().incrementFollowerCount(request.getUnfollowee().getAlias(), -1);
        } catch (DAOException e) {
            throw new RuntimeException("[DBError] Unable to update followers count for user \"" +
                    request.getUnfollowee().getAlias() + "\": " + e.getMessage());
        }

        return new UnfollowResponse(true);
    }

    public IsFollowerResponse isFollower(IsFollowerRequest request) {
        if (request.getFollower() == null) {
            throw new RuntimeException("[BadRequest] Request needs to have a follower");
        }
        else if (request.getFollowee() == null) {
            throw new RuntimeException("[BadRequest] Request needs to have a followee");
        }
        else if (request.getAuthToken() == null) {
            throw new RuntimeException("[BadRequest] Request needs to have an authToken");
        }

        try {
            if (!getUserDAO().authenticate(request.getAuthToken())) {
                return new IsFollowerResponse("Unable to authenticate. Try logging out and logging back in.");
            }
        } catch (DAOException e) {
            throw new RuntimeException("[DBError] Unable to authenticate user: " + e.getMessage());
        }

        try {
            return new IsFollowerResponse(getFollowDAO().isFollower(request.getFollowee().getAlias(), request.getFollower().getAlias()));
        } catch (DAOException e) {
            throw new RuntimeException("[DBError] Unable to determine follow relationship between \"" +
                request.getFollowee().getAlias() + "\" and \"" + request.getFollower().getAlias() + "\"");
        }
    }

    public GetFollowersCountResponse getFollowersCount(GetFollowersCountRequest request) {
        if (request.getTargetUser() == null || request.getTargetUser().getAlias() == null) {
            throw new RuntimeException("[BadRequest] Request needs to have a follower");
        }
        else if (request.getAuthToken() == null) {
            throw new RuntimeException("[BadRequest] Request needs to have an authToken");
        }

        try {
            if (!getUserDAO().authenticate(request.getAuthToken())) {
                return new GetFollowersCountResponse("Unable to authenticate. Try logging out and logging back in.");
            }
        } catch (DAOException e) {
            throw new RuntimeException("[DBError] Unable to authenticate user: " + e.getMessage());
        }

        try {
            return new GetFollowersCountResponse(getUserDAO().getFollowersCount(request.getTargetUser().getAlias()));
        } catch (DAOException e) {
            throw new RuntimeException("[DBError] Unable to get follower count: " + e.getMessage());
        }
    }

    public GetFollowingCountResponse getFollowingCount(GetFollowingCountRequest request) {
        if (request.getTargetUser() == null || request.getTargetUser().getAlias() == null) {
            throw new RuntimeException("[BadRequest] Request needs to have a follower");
        }
        else if (request.getAuthToken() == null) {
            throw new RuntimeException("[BadRequest] Request needs to have an authToken");
        }

        try {
            if (!getUserDAO().authenticate(request.getAuthToken())) {
                return new GetFollowingCountResponse("Unable to authenticate. Try logging out and logging back in.");
            }
        } catch (DAOException e) {
            throw new RuntimeException("[DBError] Unable to authenticate user: " + e.getMessage());
        }

        try {
            return new GetFollowingCountResponse(getUserDAO().getFollowingCount(request.getTargetUser().getAlias()));
        } catch (DAOException e) {
            throw new RuntimeException("[DBError] Unable to get followee count: " + e.getMessage());
        }
    }
}
