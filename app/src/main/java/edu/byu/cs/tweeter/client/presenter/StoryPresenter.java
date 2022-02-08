package edu.byu.cs.tweeter.client.presenter;

import edu.byu.cs.tweeter.client.cache.Cache;
import edu.byu.cs.tweeter.client.model.service.StoryService;
import edu.byu.cs.tweeter.client.model.service.UserService;
import edu.byu.cs.tweeter.client.presenter.view.PagedView;
import edu.byu.cs.tweeter.model.domain.Status;
import edu.byu.cs.tweeter.model.domain.User;

public class StoryPresenter extends PagedPresenter<Status> {

    private final UserService userService;
    private final StoryService storyService;

    public StoryPresenter(PagedView<Status> view) {
        super(view);
        this.userService = new UserService();
        this.storyService = new StoryService();
    }


    @Override
    protected void getItems(User user) {
        storyService.getStory(Cache.getInstance().getCurrUserAuthToken(), user, PAGE_SIZE, lastItem,
                new GetStoryObserver());
    }

    public void getUser(String userAlias) {
        userService.getUser(Cache.getInstance().getCurrUserAuthToken(), userAlias, new StoryPresenter.GetUserObserver());
        view.displayMessage("Getting user profile...");
    }

    private class GetStoryObserver extends PagedObserver implements StoryService.GetStoryObserver {
        @Override
        protected void displayErrorMessage(String message) {
            view.displayMessage("Failed to get story: " + message);
        }

        @Override
        protected void displayExceptionMessage(Exception ex) {
            view.displayMessage("Failed to get story because of exception: " + ex.getMessage());
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
