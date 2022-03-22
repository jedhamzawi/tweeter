package edu.byu.cs.tweeter.server.service;

import java.util.Random;

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
import edu.byu.cs.tweeter.server.dao.FollowDAO;
import edu.byu.cs.tweeter.server.dao.dynamo.FollowDynamoDAO;

/**
 * Contains the business logic for getting the users a user is following.
 */
public class FollowService {
    private final FollowDAO followDAO;

    @Inject
    public FollowService(FollowDAO followDAO) {
        this.followDAO = followDAO;
    }

    /**
     * Returns an instance of {@link FollowDynamoDAO}. Allows mocking of the FollowDAO class
     * for testing purposes. All usages of FollowDAO should get their FollowDAO
     * instance from this method to allow for mocking of the instance.
     *
     * @return the instance.
     */
    FollowDAO getFollowDAO() {
        return this.followDAO;
    }

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
        if(request.getTargetUser() == null || request.getTargetUser().alias == null) {
            throw new RuntimeException("[BadRequest] Request needs to have a follower alias");
        } else if(request.getLimit() <= 0) {
            throw new RuntimeException("[BadRequest] Request needs to have a positive limit");
        }

        return getFollowDAO().getFollowers(request);
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
        if(request.getTargetUser() == null || request.getTargetUser().alias == null) {
            throw new RuntimeException("[BadRequest] Request needs to have a follower alias");
        } else if(request.getLimit() <= 0) {
            throw new RuntimeException("[BadRequest] Request needs to have a positive limit");
        }

        return getFollowDAO().getFollowees(request);
    }

    public FollowResponse follow(FollowRequest request) {
        if (request.getFollowee() == null) {
            throw new RuntimeException("[BadRequest] Request needs to have a followee");
        }
        /*
        FIXME: authtoken null checking is broken with dummy data. Fix in production
        else if (request.getAuthToken()) {
            throw new RuntimeException("[BadRequest] Request needs to have an authToken");
         */

        //TODO: Update database to follow
        return getFollowDAO().follow(request);
    }

    public UnfollowResponse unfollow(UnfollowRequest request) {
        if (request.getUnfollowee() == null) {
            throw new RuntimeException("[BadRequest] Request needs to have an unfollowee");
        }
        /*
        FIXME: authtoken null checking is broken with dummy data. Fix in production
        else if (request.getAuthToken()) {
            throw new RuntimeException("[BadRequest] Request needs to have an authToken");
         */

        //TODO: Update database to follow
        return getFollowDAO().unfollow(request);
    }

    public IsFollowerResponse isFollower(IsFollowerRequest request) {
        if (request.getFollower() == null) {
            throw new RuntimeException("[BadRequest] Request needs to have a follower");
        }
        else if (request.getFollowee() == null) {
            throw new RuntimeException("[BadRequest] Request needs to have a followee");
        }
        /*
        FIXME: authtoken null checking is broken with dummy data. Fix in production
        else if (request.getAuthToken()) {
            throw new RuntimeException("[BadRequest] Request needs to have an authToken");
         */

        return getFollowDAO().isFollower(request);
    }

    public GetFollowersCountResponse getFollowersCount(GetFollowersCountRequest request) {
        if (request.getTargetUser() == null) {
            throw new RuntimeException("[BadRequest] Request needs to have a follower");
        }
        /*
        FIXME: authtoken null checking is broken with dummy data. Fix in production
        else if (request.getAuthToken()) {
            throw new RuntimeException("[BadRequest] Request needs to have an authToken");
         */
        return getFollowDAO().getFollowersCount(request);
    }

    public GetFollowingCountResponse getFollowingCount(GetFollowingCountRequest request) {
        if (request.getTargetUser() == null) {
            throw new RuntimeException("[BadRequest] Request needs to have a follower");
        }
        /*
        FIXME: authtoken null checking is broken with dummy data. Fix in production
        else if (request.getAuthToken()) {
            throw new RuntimeException("[BadRequest] Request needs to have an authToken");
         */

        //TODO: Get real count from db
        return getFollowDAO().getFollowingCount(request);
    }
}
