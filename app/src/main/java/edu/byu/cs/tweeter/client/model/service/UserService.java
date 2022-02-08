package edu.byu.cs.tweeter.client.model.service;

import android.os.Message;

import edu.byu.cs.tweeter.client.cache.Cache;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.GetUserTask;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.LoginTask;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.LogoutTask;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.RegisterTask;
import edu.byu.cs.tweeter.client.model.service.handler.ServiceHandler;
import edu.byu.cs.tweeter.client.model.service.observer.ServiceObserver;
import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.User;

public class UserService extends Service {
    public interface GetUserObserver extends ServiceObserver { void handleSuccess(User user); }
    public interface LoginObserver extends ServiceObserver { void handleSuccess(User loggedInUser); }
    public interface LogoutObserver extends ServiceObserver { void handleSuccess(); }
    public interface RegisterObserver extends ServiceObserver { void handleSuccess(User registeredUser); }

    public void getUser(AuthToken currUserAuthToken, String userAlias, GetUserObserver getUserObserver) {
        executeTask(new GetUserTask(currUserAuthToken, userAlias, new GetUserHandler(getUserObserver)));
    }

    public void loginUser(String alias, String password, LoginObserver loginObserver) {
        executeTask(new LoginTask(alias, password, new LoginHandler(loginObserver)));
    }

    public void logoutUser(AuthToken authToken, LogoutObserver observer) {
        executeTask(new LogoutTask(authToken, new LogoutHandler(observer)));
    }

    public void registerUser(String imageBytesBase64, String firstName, String lastName, String alias, String password, RegisterObserver observer) {
        executeTask(new RegisterTask(firstName, lastName, alias, password, imageBytesBase64, new RegisterHandler(observer)));
    }

    /**
     * Message handler (i.e., observer) for GetUserTask.
     */
    private class GetUserHandler extends ServiceHandler {
        private final GetUserObserver observer;

        GetUserHandler(GetUserObserver observer) {
            this.observer = observer;
        }

        @Override
        public void handleSuccess(Message msg) {
            User user = (User) msg.getData().getSerializable(GetUserTask.USER_KEY);
            observer.handleSuccess(user);
        }
        @Override
        public void handleFailure(String message) {
            observer.handleFailure(message);
        }
        @Override
        public void handleException(Exception ex) {
            observer.handleException(ex);
        }
    }

    private class LoginHandler extends ServiceHandler {
        private final LoginObserver observer;

        public LoginHandler(LoginObserver observer) {
            this.observer = observer;
        }

        @Override
        public void handleSuccess(Message msg) {
            User loggedInUser = (User) msg.getData().getSerializable(LoginTask.USER_KEY);
            AuthToken authToken = (AuthToken) msg.getData().getSerializable(LoginTask.AUTH_TOKEN_KEY);

            // Cache user session information
            Cache.getInstance().setCurrUser(loggedInUser);
            Cache.getInstance().setCurrUserAuthToken(authToken);

            observer.handleSuccess(loggedInUser);
        }
        @Override
        public void handleFailure(String message) {
            observer.handleFailure(message);
        }
        @Override
        public void handleException(Exception ex) {
            observer.handleException(ex);
        }
    }

    private class LogoutHandler extends ServiceHandler {
        private final LogoutObserver observer;

        public LogoutHandler(LogoutObserver observer) {
            this.observer = observer;
        }

        @Override
        public void handleSuccess(Message msg) {
            observer.handleSuccess();
        }
        @Override
        public void handleFailure(String message) {
            observer.handleFailure(message);
        }
        @Override
        public void handleException(Exception ex) {
            observer.handleException(ex);
        }

    }

    private class RegisterHandler extends ServiceHandler {
        private final RegisterObserver observer;

        public RegisterHandler(RegisterObserver observer) {
            this.observer = observer;
        }

        @Override
        public void handleSuccess(Message msg) {
            User registeredUser = (User) msg.getData().getSerializable(RegisterTask.USER_KEY);
            AuthToken authToken = (AuthToken) msg.getData().getSerializable(RegisterTask.AUTH_TOKEN_KEY);

            Cache.getInstance().setCurrUser(registeredUser);
            Cache.getInstance().setCurrUserAuthToken(authToken);

            observer.handleSuccess(registeredUser);
        }
        @Override
        public void handleFailure(String message) {
            observer.handleFailure(message);
        }
        @Override
        public void handleException(Exception ex) {
            observer.handleException(ex);
        }
    }
}
