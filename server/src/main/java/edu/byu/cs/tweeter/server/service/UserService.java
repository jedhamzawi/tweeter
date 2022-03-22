package edu.byu.cs.tweeter.server.service;

import javax.inject.Inject;

import edu.byu.cs.tweeter.model.net.request.LoginRequest;
import edu.byu.cs.tweeter.model.net.request.LogoutRequest;
import edu.byu.cs.tweeter.model.net.request.RegisterRequest;
import edu.byu.cs.tweeter.model.net.request.UserRequest;
import edu.byu.cs.tweeter.model.net.response.LoginResponse;
import edu.byu.cs.tweeter.model.net.response.LogoutResponse;
import edu.byu.cs.tweeter.model.net.response.RegisterResponse;
import edu.byu.cs.tweeter.model.net.response.UserResponse;
import edu.byu.cs.tweeter.server.dao.UserDAO;

public class UserService {
    private final UserDAO userDAO;

    @Inject
    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public UserDAO getUserDAO() { return this.userDAO; }

    public LoginResponse login(LoginRequest request) {
        if(request.getUsername() == null){
            throw new RuntimeException("[BadRequest] Missing a username");
        } else if(request.getPassword() == null) {
            throw new RuntimeException("[BadRequest] Missing a password");
        }

        // TODO: Generates dummy data. Replace with a real implementation.
        return getUserDAO().login(request);
    }

    public LogoutResponse logout(LogoutRequest request) {
        /*
        FIXME: Null checking is broken using dummy data. Add back in for production
        if(request.getAuthToken() == null) {
            throw new RuntimeException("[BadRequest] Missing an authToken");
        }
         */

        // TODO: Delete authToken from db
        return getUserDAO().logout(request);
    }

    public RegisterResponse register(RegisterRequest request) {
        if(request.getUsername() == null){
            throw new RuntimeException("[BadRequest] Missing a username");
        } else if(request.getPassword() == null) {
            throw new RuntimeException("[BadRequest] Missing a password");
        }

        // TODO: Generates dummy data. Replace with a real implementation.
        return getUserDAO().register(request);
    }

    public UserResponse getUser(UserRequest request) {
        if(request.getUsername() == null) {
            throw new RuntimeException("[BadRequest] Missing a username");
        }
        /*
        FIXME: Null checking is broken using dummy data. Add back in for production
        else if(request.getAuthToken() == null) {
            throw new RuntimeException("[BadRequest] Missing an authToken");
        }
         */

        // TODO: Generates dummy data. Replace with a real implementation.
        return getUserDAO().getUser(request);
    }
}
