package edu.byu.cs.tweeter.client.presenter;


import android.widget.Toast;

import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.byu.cs.tweeter.client.cache.Cache;
import edu.byu.cs.tweeter.client.model.service.FollowService;
import edu.byu.cs.tweeter.client.model.service.StatusService;
import edu.byu.cs.tweeter.client.model.service.UserService;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.LogoutTask;
import edu.byu.cs.tweeter.client.view.main.MainActivity;
import edu.byu.cs.tweeter.model.domain.User;

public class MainPresenter {

    public interface View {
        void displayMessage(String message);
        void updateFollower(boolean isFollower);
        void updateFollow(boolean follow);
        void updateUnfollow(boolean unfollow);
        void logoutUser();
        void cancelPostingTost();
    }

    private final View view;
    private FollowService followService;
    private UserService userService;
    private StatusService statusService;

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

    public class IsFollowerObserver implements FollowService.IsFollowerObserver {
        @Override
        public void handleSuccess(boolean isFollower) {
            view.updateFollower(isFollower);
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
            view.cancelPostingTost();
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
}
