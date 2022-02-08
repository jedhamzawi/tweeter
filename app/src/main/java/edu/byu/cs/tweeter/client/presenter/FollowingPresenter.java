package edu.byu.cs.tweeter.client.presenter;

import java.util.List;

import edu.byu.cs.tweeter.client.cache.Cache;
import edu.byu.cs.tweeter.client.model.service.FollowService;
import edu.byu.cs.tweeter.client.model.service.UserService;
import edu.byu.cs.tweeter.client.presenter.view.PagedView;
import edu.byu.cs.tweeter.model.domain.User;

public class FollowingPresenter extends PagedPresenter<User> {

    private final FollowService followService;
    private final UserService userService;

    public FollowingPresenter(PagedView<User> view) {
        super(view);
        followService = new FollowService();
        userService = new UserService();
    }

    public boolean hasMorePages() {
        return hasMorePages;
    }

    public void setHasMorePages(boolean hasMorePages) {
        this.hasMorePages = hasMorePages;
    }

    public boolean isLoading() {
        return isLoading;
    }

    @Override
    protected void getItems(User user) {
        followService.getFollowing(Cache.getInstance().getCurrUserAuthToken(), user, PAGE_SIZE,
                lastItem, new GetFollowingObserver());
    }

    public void getUser(String userAlias) {
        userService.getUser(Cache.getInstance().getCurrUserAuthToken(), userAlias, new GetUserObserver());
        view.displayMessage("Getting user's profile...");
    }

    private class GetFollowingObserver extends PagedObserver implements FollowService.GetFollowingObserver {
        @Override
        protected void displayErrorMessage(String message) {
            view.displayMessage("Failed to get following: " + message);
        }

        @Override
        protected void displayExceptionMessage(Exception ex) {
            view.displayMessage("Failed to get following because of exception: " + ex.getMessage());
        }
    }

    private class GetUserObserver implements UserService.GetUserObserver {
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
