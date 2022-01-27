package edu.byu.cs.tweeter.client.presenter;

import java.net.MalformedURLException;
import java.text.ParseException;

import edu.byu.cs.tweeter.client.cache.Cache;
import edu.byu.cs.tweeter.client.model.service.FollowService;
import edu.byu.cs.tweeter.client.model.service.StatusService;
import edu.byu.cs.tweeter.client.model.service.UserService;
import edu.byu.cs.tweeter.model.domain.User;

public class MainPresenter {

    public interface View {
        void displayMessage(String message);
        void updateFollowButton(boolean isFollower);
        void updateFollow(boolean follow);
        void updateUnfollow(boolean unfollow);
        void logoutUser();
        void cancelPostingToast();
        void updateFollowersCount(int count);
        void updateFollowingCount(int count);
    }

    private final View view;
    private final FollowService followService;
    private final UserService userService;
    private final StatusService statusService;

    public MainPresenter(View view) {
        this.view = view;
        followService = new FollowService();
        userService = new UserService();
        statusService = new StatusService();
    }

    public void isFollower(User selectedUser) {
        followService.isFollower(Cache.getInstance().getCurrUserAuthToken(), Cache.getInstance().getCurrUser(),
                selectedUser, new IsFollowerObserver());
    }

    public void follow(User selectedUser) {
        view.displayMessage("Adding " + selectedUser.getName() + "...");
        followService.follow(Cache.getInstance().getCurrUserAuthToken(), selectedUser, new FollowObserver());
    }

    public void unfollow(User selectedUser) {
        view.displayMessage("Removing " + selectedUser.getName() + "...");
        followService.unfollow(Cache.getInstance().getCurrUserAuthToken(), selectedUser, new UnfollowObserver());
    }

    public void logoutUser() {
        userService.logoutUser(Cache.getInstance().getCurrUserAuthToken(), new LogoutObserver());
    }

    public void postStatus(String post) throws ParseException, MalformedURLException {
        statusService.postStatus(post, Cache.getInstance().getCurrUser(), new PostStatusObserver());
    }

    public void getFollowersCount(User selectedUser) {
        followService.getFollowersCount(Cache.getInstance().getCurrUserAuthToken(), selectedUser, new GetFollowersCountObserver());
    }

    public void getFollowingCount(User selectedUser) {
        followService.getFollowingCount(Cache.getInstance().getCurrUserAuthToken(), selectedUser, new GetFollowingCountObserver());
    }

    public class IsFollowerObserver implements FollowService.IsFollowerObserver {
        @Override
        public void handleSuccess(boolean isFollower) {
            view.updateFollowButton(isFollower);
        }

        @Override
        public void handleFailure(String message) {
            view.displayMessage("Failed to determine following relationship: " + message);
        }

        @Override
        public void handleException(Exception ex) {
            view.displayMessage("Failed to determine following relationship because of exception: " + ex.getMessage());
        }
    }

    public class FollowObserver implements FollowService.FollowObserver {
        @Override
        public void handleSuccess() {
            view.updateFollow(true);
        }

        @Override
        public void handleFailure(String message) {
            view.displayMessage("Failed to follow: " + message);
            view.updateFollow(false);
        }

        @Override
        public void handleException(Exception ex) {
            view.displayMessage("Failed to follow because of exception: " + ex.getMessage());
            view.updateFollow(false);
        }
    }

    public class UnfollowObserver implements FollowService.UnfollowObserver {
        @Override
        public void handleSuccess() {
            view.updateUnfollow(true);
        }

        @Override
        public void handleFailure(String message) {
            view.updateUnfollow(false);
            view.displayMessage("Failed to unfollow: " + message);
        }

        @Override
        public void handleException(Exception ex) {
            view.updateUnfollow(false);
            view.displayMessage("Failed to unfollow because of exception: " + ex.getMessage());
        }
    }

    public class LogoutObserver implements UserService.LogoutObserver {
        @Override
        public void handleSuccess() {
            Cache.getInstance().clearCache();
            view.logoutUser();
        }

        @Override
        public void handleFailure(String message) {
            view.displayMessage("Failed to logout: " + message);
        }

        @Override
        public void handleException(Exception ex) {
            view.displayMessage("Failed to logout because of exception: " + ex.getMessage());
        }
    }

    public class PostStatusObserver implements StatusService.PostStatusObserver {
        @Override
        public void handleSuccess() {
            view.cancelPostingToast();
        }

        @Override
        public void handleFailure(String message) {
            view.displayMessage("Failed to post status: " + message);
        }

        @Override
        public void handleException(Exception ex) {
            view.displayMessage("Failed to post status because of exception: " + ex.getMessage());
        }
    }

    public class GetFollowersCountObserver implements FollowService.GetFollowersCountObserver {
        @Override
        public void handleSuccess(int count) {
            view.updateFollowersCount(count);
        }

        @Override
        public void handleFailure(String message) {
            view.displayMessage("Failed to get followers count: " + message);
        }

        @Override
        public void handleException(Exception ex) {
            view.displayMessage("Failed to get followers count because of exception: " + ex.getMessage());
        }
    }

    public class GetFollowingCountObserver implements FollowService.GetFollowingCountObserver {
        @Override
        public void handleSuccess(int count) {
            view.updateFollowingCount(count);
        }

        @Override
        public void handleFailure(String message) {
            view.displayMessage("Failed to get following count: " + message);
        }

        @Override
        public void handleException(Exception ex) {
            view.displayMessage("Failed to get following count because of exception: " + ex.getMessage());
        }
    }

}
