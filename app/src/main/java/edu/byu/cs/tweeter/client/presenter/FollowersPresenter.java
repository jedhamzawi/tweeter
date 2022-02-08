package edu.byu.cs.tweeter.client.presenter;


import edu.byu.cs.tweeter.client.cache.Cache;
import edu.byu.cs.tweeter.client.model.service.FollowService;
import edu.byu.cs.tweeter.client.model.service.UserService;
import edu.byu.cs.tweeter.client.presenter.view.PagedView;
import edu.byu.cs.tweeter.model.domain.User;

public class FollowersPresenter extends PagedPresenter<User> {

    private final UserService userService;
    private final FollowService followService;

    public FollowersPresenter(PagedView<User> view) {
        super(view);
        this.userService = new UserService();
        this.followService = new FollowService();
    }

    @Override
    protected void getItems(User user) {
        followService.getFollowers(Cache.getInstance().getCurrUserAuthToken(), user,
                PAGE_SIZE, lastItem, new GetFollowersObserver());
    }

    public void getUser(String userAlias) {
        userService.getUser(Cache.getInstance().getCurrUserAuthToken(), userAlias, new GetUserObserver());
        view.displayMessage("Getting user profile...");
    }

    private class GetFollowersObserver extends PagedObserver implements FollowService.GetFollowersObserver {
        @Override
        protected void displayErrorMessage(String message) {
            view.displayMessage("Failed to get followers: " + message);
        }

        @Override
        protected void displayExceptionMessage(Exception ex) {
            view.displayMessage("Failed to get followers because of exception: " + ex.getMessage());
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
