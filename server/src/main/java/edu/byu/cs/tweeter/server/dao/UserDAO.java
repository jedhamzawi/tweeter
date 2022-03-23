package edu.byu.cs.tweeter.server.dao;

import edu.byu.cs.tweeter.model.net.request.LoginRequest;
import edu.byu.cs.tweeter.model.net.request.LogoutRequest;
import edu.byu.cs.tweeter.model.net.request.RegisterRequest;
import edu.byu.cs.tweeter.model.net.request.UserRequest;
import edu.byu.cs.tweeter.model.net.response.LoginResponse;
import edu.byu.cs.tweeter.model.net.response.LogoutResponse;
import edu.byu.cs.tweeter.model.net.response.RegisterResponse;
import edu.byu.cs.tweeter.model.net.response.UserResponse;

public interface UserDAO {
    LoginResponse login(LoginRequest login) throws DAOException;
    LogoutResponse logout(LogoutRequest request) throws DAOException;
    RegisterResponse register(RegisterRequest request) throws DAOException;
    UserResponse getUser(UserRequest request) throws DAOException;
}
