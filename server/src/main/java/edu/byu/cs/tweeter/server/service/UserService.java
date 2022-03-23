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
import edu.byu.cs.tweeter.server.dao.DAOException;
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

        try {
            return getUserDAO().login(request);
        } catch (DAOException e) {
            throw new RuntimeException("[DBError] " + e.getMessage());
        }
    }

    public LogoutResponse logout(LogoutRequest request) {
        if (request.getAuthToken() == null) {
            throw new RuntimeException("[BadRequest] Missing an authToken");
        }

        try {
            return getUserDAO().logout(request);
        } catch (DAOException e) {
            throw new RuntimeException("[DBError] " + e.getMessage());
        }
    }

    public RegisterResponse register(RegisterRequest request) {
        if(request.getUsername() == null){
            throw new RuntimeException("[BadRequest] Missing a username");
        } else if(request.getPassword() == null) {
            throw new RuntimeException("[BadRequest] Missing a password");
        }

        try {
            return getUserDAO().register(request);
        } catch (DAOException e) {
            throw new RuntimeException("[DBError] " + e.getMessage());
        }
    }

    public UserResponse getUser(UserRequest request) {
        if(request.getUsername() == null) {
            throw new RuntimeException("[BadRequest] Missing a username");
        }
        else if(request.getAuthToken() == null) {
            throw new RuntimeException("[BadRequest] Missing an authToken");
        }

        try {
            return getUserDAO().getUser(request);
        } catch (DAOException e) {
            throw new RuntimeException("[DBError] " + e.getMessage());
        }
    }
}
