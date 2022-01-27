package edu.byu.cs.tweeter.client.presenter;


import edu.byu.cs.tweeter.client.cache.Cache;
import edu.byu.cs.tweeter.client.model.service.FollowService;
import edu.byu.cs.tweeter.model.domain.User;

public class MainPresenter {
    public interface View {
        void displayMessage(String message);
        void updateFollower(boolean isFollower);
        void updateFollow(boolean follow);
        void updateUnfollow(boolean unfollow);
    }

    private final View view;
    private FollowService followService;

    public MainPresenter(View view) {
        this.view = view;
        followService = new FollowService();
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
}
