package edu.byu.cs.tweeter.client.presenter;

import java.util.List;

import edu.byu.cs.tweeter.client.cache.Cache;
import edu.byu.cs.tweeter.client.model.service.FollowService;
import edu.byu.cs.tweeter.client.model.service.UserService;
import edu.byu.cs.tweeter.model.domain.User;

public class FollowersPresenter {
    private static final int PAGE_SIZE = 10;

    public interface View {
        void displayMessage(String message);
        void newUserActivity(User user);
        void setLoadingStatus(boolean value);
        void addFollowers(List<User> followers);
    }

    private final View view;
    private final UserService userService;
    private final FollowService followService;

    private boolean isLoading = false;
    private boolean hasMorePages;
    private User lastFollower;

    public FollowersPresenter(View view) {
        this.view = view;
        this.userService = new UserService();
        this.followService = new FollowService();
    }

    public boolean isLoading() {
        return isLoading;
    }

    public void setLoading(boolean loading) {
        isLoading = loading;
    }

    public boolean hasMorePages() {
        return hasMorePages;
    }

    public void setHasMorePages(boolean hasMorePages) {
        this.hasMorePages = hasMorePages;
    }

    public void getFollowers(User user) {
        if (!isLoading) {   // This guard is important for avoiding a race condition in the scrolling code.
            isLoading = true;
            view.setLoadingStatus(true);
            followService.getFollowers(Cache.getInstance().getCurrUserAuthToken(), user,
                    PAGE_SIZE, lastFollower, new GetFollowersObserver());
        }
    }

    public void getUser(String userAlias) {
        userService.getUser(Cache.getInstance().getCurrUserAuthToken(), userAlias, new GetUserObserver());
        view.displayMessage("Getting user profile...");
    }

    public class GetFollowersObserver implements FollowService.GetFollowersObserver {
        @Override
        public void handleSuccess(List<User> followers, boolean hasMorePages) {
            isLoading = false;
            view.setLoadingStatus(false);
            setHasMorePages(hasMorePages);
            lastFollower = (followers.size() > 0) ? followers.get(followers.size() - 1) : null;
            view.addFollowers(followers);
        }

        @Override
        public void handleFailure(String message) {
            isLoading = false;
            view.setLoadingStatus(false);
            view.displayMessage("Failed to get followers: " + message);
        }

        @Override
        public void handleException(Exception ex) {
            isLoading = false;
            view.setLoadingStatus(false);
            view.displayMessage("Failed to get followers because of exception: " + ex.getMessage());
        }
    }

    public class GetUserObserver implements UserService.GetUserObserver {
        @Override
        public void handleSuccess(User user) {
            view.newUserActivity(user);
        }

        @Override
        public void handleFailure(String message) {
            view.displayMessage("Failed to get user's profile: " + message);
        }

        @Override
        public void handleException(Exception ex) {
            view.displayMessage("Failed to get user's profile because of exception: " + ex.getMessage());
        }
    }
}
