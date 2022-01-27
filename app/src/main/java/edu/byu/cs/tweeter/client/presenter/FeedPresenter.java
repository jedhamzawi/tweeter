package edu.byu.cs.tweeter.client.presenter;

import java.net.MalformedURLException;
import java.util.List;

import edu.byu.cs.tweeter.client.cache.Cache;
import edu.byu.cs.tweeter.client.model.service.FeedService;
import edu.byu.cs.tweeter.client.model.service.UserService;
import edu.byu.cs.tweeter.model.domain.Status;
import edu.byu.cs.tweeter.model.domain.User;

public class FeedPresenter {

    public interface View {
        void setLoadingStatus(boolean value) throws MalformedURLException;
        void displayMessage(String message);
        void newUserActivity(User user);
        void addStatuses(List<Status> statuses);
    }

    private final View view;
    private final UserService userService;
    private final FeedService feedService;

    private boolean hasMorePages;
    private boolean isLoading = false;
    private Status lastStatus;
    private static final int PAGE_SIZE = 10;

    public FeedPresenter(View view) {
        this.view = view;
        this.userService = new UserService();
        this.feedService = new FeedService();
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

    public void loadMoreItems(User user) throws MalformedURLException {
        if (!isLoading) {   // This guard is important for avoiding a race condition in the scrolling code.
            isLoading = true;
            view.setLoadingStatus(true);
            feedService.getFeed(Cache.getInstance().getCurrUserAuthToken(), user, PAGE_SIZE, lastStatus,
                    new GetFeedObserver());

        }
    }

    public void getUser(String userAlias) {
        userService.getUser(Cache.getInstance().getCurrUserAuthToken(), userAlias, new GetUserObserver());
        view.displayMessage("Getting user's profile...");
    }

    private class GetFeedObserver implements FeedService.GetFeedObserver {
        @Override
        public void handleSuccess(List<Status> statuses, boolean hasMorePages) {
            isLoading = false;
            try {
                view.setLoadingStatus(false);
            } catch (MalformedURLException ex) {
                ex.printStackTrace();
            }
            lastStatus = (statuses.size() > 0) ? statuses.get(statuses.size() - 1) : null;
            setHasMorePages(hasMorePages);

            view.addStatuses(statuses);
        }

        @Override
        public void handleFailure(String message) {
            isLoading = false;
            try {
                view.setLoadingStatus(false);
            } catch (MalformedURLException ex) {
                ex.printStackTrace();
            }
            view.displayMessage("Failed to get feed: " + message);
        }

        @Override
        public void handleException(Exception ex) {
            isLoading = false;
            try {
                view.setLoadingStatus(false);
            } catch (MalformedURLException urlEx) {
                urlEx.printStackTrace();
            }
            view.displayMessage("Failed to get feed because of exception: " + ex.getMessage());
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
