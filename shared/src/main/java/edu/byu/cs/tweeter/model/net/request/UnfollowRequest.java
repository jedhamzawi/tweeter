package edu.byu.cs.tweeter.model.net.request;

import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.User;

public class UnfollowRequest implements Request {
    private User unfollowee;
    private AuthToken authToken;

    /**
     * Allows construction of the object from Json. Private so it won't be called in normal code.
     */
    private UnfollowRequest() {}

    public UnfollowRequest(User unfollowee, AuthToken authToken) {
        this.unfollowee = unfollowee;
        this.authToken = authToken;
    }

    public AuthToken getAuthToken() {
        return authToken;
    }

    public void setAuthToken(AuthToken authToken) {
        this.authToken = authToken;
    }

    public User getUnfollowee() {
        return unfollowee;
    }

    public void setUnfollowee(User followee) {
        this.unfollowee = followee;
    }
}
