package edu.byu.cs.tweeter.client.presenter;

import edu.byu.cs.tweeter.client.model.service.UserService;
import edu.byu.cs.tweeter.model.domain.User;

public class LoginPresenter {

    public interface View {
        void loginUser(User loggedInUser);
        void displayMessage(String message);
    }

    private final View view;
    private final UserService userService;

    public LoginPresenter(View view) {
        this.view = view;
        this.userService = new UserService();
    }

    public void loginUser(String alias, String password) {
        // Send the login request.
        userService.loginUser(alias, password, new LoginObserver());
    }

    private class LoginObserver implements UserService.LoginObserver {

        @Override
        public void handleSuccess(User loggedInUser) {
            view.loginUser(loggedInUser);
        }

        @Override
        public void handleFailure(String message) {
            view.displayMessage("Failed to login: " + message);
        }

        @Override
        public void handleException(Exception ex) {
            view.displayMessage("Failed to login because of exception: " + ex.getMessage());
        }
    }
}
