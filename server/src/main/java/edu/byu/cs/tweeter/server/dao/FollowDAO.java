package edu.byu.cs.tweeter.server.dao;

import edu.byu.cs.tweeter.model.net.request.GetFollowersCountRequest;
import edu.byu.cs.tweeter.model.net.request.GetFollowersRequest;
import edu.byu.cs.tweeter.model.net.request.GetFollowingCountRequest;
import edu.byu.cs.tweeter.model.net.request.GetFollowingRequest;
import edu.byu.cs.tweeter.model.net.response.GetFollowersCountResponse;
import edu.byu.cs.tweeter.model.net.response.GetFollowersResponse;
import edu.byu.cs.tweeter.model.net.response.GetFollowingCountResponse;
import edu.byu.cs.tweeter.model.net.response.GetFollowingResponse;

public interface FollowDAO {
    GetFollowingCountResponse getFollowingCount(GetFollowingCountRequest request);
    GetFollowersCountResponse getFollowersCount(GetFollowersCountRequest request);
    GetFollowingResponse getFollowees(GetFollowingRequest request);
    GetFollowersResponse getFollowers(GetFollowersRequest request);
}
