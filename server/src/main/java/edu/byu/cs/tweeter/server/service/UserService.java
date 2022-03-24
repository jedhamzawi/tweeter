package edu.byu.cs.tweeter.server.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.model.ObjectMetadata;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import javax.inject.Inject;

import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.model.net.request.LoginRequest;
import edu.byu.cs.tweeter.model.net.request.LogoutRequest;
import edu.byu.cs.tweeter.model.net.request.RegisterRequest;
import edu.byu.cs.tweeter.model.net.request.UserRequest;
import edu.byu.cs.tweeter.model.net.response.LoginResponse;
import edu.byu.cs.tweeter.model.net.response.LogoutResponse;
import edu.byu.cs.tweeter.model.net.response.RegisterResponse;
import edu.byu.cs.tweeter.model.net.response.UserResponse;
import edu.byu.cs.tweeter.server.dao.DAOException;
import edu.byu.cs.tweeter.server.dao.DBUserData;
import edu.byu.cs.tweeter.server.dao.UserDAO;

public class UserService {
    private static final String IMAGE_METADATA = "image/png";
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    
    private final UserDAO userDAO;

    @Inject
    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public UserDAO getUserDAO() { return this.userDAO; }

    public LoginResponse login(LoginRequest request) {
        if (request.getUsername() == null){
            throw new RuntimeException("[BadRequest] Missing a username");
        } else if (request.getPassword() == null) {
            throw new RuntimeException("[BadRequest] Missing a password");
        }

        DBUserData userData;
        try {
            userData = getUserDAO().getUser(request.getUsername());
        } catch (DAOException e) {
            throw new RuntimeException("[DBError] Checking for user in db failed: " + e.getMessage());
        }

        if (userData == null) {
            return new LoginResponse("Invalid login credentials! Please try again.");
        }

        try {
            if (!validatePassword(request.getPassword(), userData.getHashedPassword(), userData.getSalt())) {
                return new LoginResponse("Invalid login credentials! Try again");
            }
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("[ServiceError] Unable to validate password: " + e.getMessage());
        }

        AuthToken authToken = generateAuthToken();
        try {
            getUserDAO().putAuthToken(authToken);
        } catch (DAOException e) {
            throw new RuntimeException("[DBError] Unable to put authToken in table: " + e.getMessage());
        }

        return new LoginResponse(userData.getUser(), authToken);
    }

    public LogoutResponse logout(LogoutRequest request) {
        if (request.getAuthToken() == null) {
            throw new RuntimeException("[BadRequest] Missing an authToken");
        }

        try {
            getUserDAO().deleteAuthToken(request.getAuthToken());
        } catch (DAOException e) {
            throw new RuntimeException("[DBError] Unable to delete authToken: " + e.getMessage());
        }

        return new LogoutResponse(true);
    }

    public RegisterResponse register(RegisterRequest request) {
        if(request.getUsername() == null){
            throw new RuntimeException("[BadRequest] Missing a username");
        } else if(request.getPassword() == null) {
            throw new RuntimeException("[BadRequest] Missing a password");
        }

        try {
            if (getUserDAO().getUser(request.getUsername()) != null) {
                return new RegisterResponse("User already exists! Choose a different alias.");
            }
        } catch (DAOException e) {
            throw new RuntimeException("[DBError] Unable to check if user already exists: " + e.getMessage());
        }

        String hashedPassword;
        String salt;
        try {
            hashedPassword = hashPassword(request.getPassword());
            salt = getSalt();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("[ServiceError] Unable to hash password: " + e.getMessage());
        }

        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(IMAGE_METADATA);
        String imageURL;
        try {
            imageURL = getUserDAO().uploadImage(request.getImage(), request.getUsername(), objectMetadata);
        } catch (DAOException e) {
            throw new RuntimeException("[DBError] Unable to upload image: " + e.getMessage());
        }

        try {
            getUserDAO().putUser(request.getUsername(), hashedPassword, salt, request.getFirstName(),
                    request.getLastName(), imageURL, 0, 0);
            System.out.println("Successfully put user in table");
        } catch (DAOException e) {
            throw new RuntimeException("[DBError] Unable to put user in table: " + e.getMessage());
        }

        AuthToken authToken = generateAuthToken();
        try {
            getUserDAO().putAuthToken(authToken);
            System.out.println("Successfully put authToken in table");
        } catch (DAOException e) {
            throw new RuntimeException("[DBError] Unable to put authToken in table: " + e.getMessage());
        }

        return new RegisterResponse(new User(request.getFirstName(), request.getLastName(), request.getUsername(), imageURL), authToken);
    }

    public UserResponse getUser(UserRequest request) {
        if (request.getUsername() == null) {
            throw new RuntimeException("[BadRequest] Missing a username");
        }
        else if (request.getAuthToken() == null) {
            throw new RuntimeException("[BadRequest] Missing an authToken");
        }

        try {
            if (!getUserDAO().authenticate(request.getAuthToken())) {
                return new UserResponse("Unable to authenticate logged-in user. Try logging out and logging back in.");
            }
        } catch (DAOException e) {
            throw new RuntimeException("[DBError] Unable to authenticate logged-in user: " + e.getMessage());
        }

        DBUserData userData;
        try {
            userData = getUserDAO().getUser(request.getUsername());
        } catch (AmazonServiceException | DAOException e) {
            throw new RuntimeException("[DBError] Unable to find user \"" + request.getUsername() + "\": " + e.getMessage());
        }
        
        if (userData == null) {
            return new UserResponse("User \"" + request.getUsername() + "\" not found!");
        }
        
        return new UserResponse(userData.getUser());
    }

    private String hashPassword(String passwordToHash) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(passwordToHash.getBytes());
        byte[] bytes = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte aByte : bytes) {
            sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }

    private String getSalt() throws NoSuchAlgorithmException {
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        byte[] salt = new byte[16];
        sr.nextBytes(salt);
        return new String(salt, StandardCharsets.UTF_8);
    }

    private boolean validatePassword(String passwordFromClient, String passwordFromDB, String salt) throws NoSuchAlgorithmException {
        String hashedClientPassword = hashPassword(passwordFromClient);
        return passwordFromDB.equals(hashedClientPassword + salt);
    }

    private AuthToken generateAuthToken() {
        return new AuthToken(UUID.randomUUID().toString(), LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)));
    }
}
