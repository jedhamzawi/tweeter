package edu.byu.cs.tweeter.client.presenter;

import android.graphics.Bitmap;

import edu.byu.cs.tweeter.client.model.service.UserService;
import edu.byu.cs.tweeter.model.domain.User;

public class RegisterPresenter {
    public interface View {
        void registerUser(User registeredUser);
        void displayMessage(String message);
    }

    private final View view;
    private final UserService userService;

    public RegisterPresenter(View view) {
        this.view = view;
        this.userService = new UserService();
    }

    public void registerUser(Bitmap image, String firstName, String lastName, String alias, String password) {
        userService.registerUser(image, firstName, lastName, alias, password, new RegisterObserver());
    }

    private class RegisterObserver implements UserService.RegisterObserver {
        @Override
        public void handleSuccess(User registeredUser) {
            view.registerUser(registeredUser);
        }

        @Override
        public void handleFailure(String message) {
            view.displayMessage("Failed to register: " + message);
        }

        @Override
        public void handleException(Exception ex) {
            view.displayMessage("Failed to register because of exception: " + ex.getMessage());
        }
    }
}
