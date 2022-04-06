package edu.byu.cs.tweeter.server.service;

import com.amazonaws.services.s3.model.ObjectMetadata;

import org.checkerframework.checker.units.qual.A;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
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
import edu.byu.cs.tweeter.server.dao.model.UserDBData;
import edu.byu.cs.tweeter.server.dao.UserDAO;

public class UserService extends Service {
    private static final String IMAGE_METADATA = "image/png";
    
    private final UserDAO userDAO;

    @Inject
    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    public UserDAO getUserDAO() { return this.userDAO; }

    public LoginResponse login(LoginRequest request) {
        if (request.getUsername() == null){
            throw new RuntimeException("[BadRequest] Missing a username");
        } else if (request.getPassword() == null) {
            throw new RuntimeException("[BadRequest] Missing a password");
        }

        UserDBData userData;
        try {
            userData = getUserDAO().getUser(request.getUsername());
        } catch (Exception e) {
            e.printStackTrace();
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
        } catch (Exception e) {
            e.printStackTrace();
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
        } catch (Exception e) {
            e.printStackTrace();
            // Logout the user anyway, leaving dangling authToken
            return new LogoutResponse(true);
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
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("[DBError] Unable to check if user already exists: " + e.getMessage());
        }

        String hashedPassword;
        String salt;
        try {
            salt = getSalt();
            hashedPassword = hashPassword(salt + request.getPassword());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new RuntimeException("[ServiceError] Unable to hash password: " + e.getMessage());
        }

        // Default image url
        String imageURL = "https://hamsesh-tweeter.s3.us-east-2.amazonaws.com/test.png";
        if (request.getImage() != null) {
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentType(IMAGE_METADATA);
            try {
                imageURL = getUserDAO().uploadImage(new ByteArrayInputStream(request.getImage()), request.getUsername(), objectMetadata);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("[DBError] Unable to upload image: " + e.getMessage());
            }
        }

        try {
            getUserDAO().putUser(request.getUsername(), hashedPassword, salt, request.getFirstName(),
                    request.getLastName(), imageURL, 0, 0);
            System.out.println("Successfully put user in table");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("[DBError] Unable to put user in table: " + e.getMessage());
        }

        AuthToken authToken = generateAuthToken();
        try {
            getUserDAO().putAuthToken(authToken);
            System.out.println("Successfully put authToken in table");
        } catch (Exception e) {
            e.printStackTrace();
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

        if (!authenticate(request.getAuthToken())) {
            return new UserResponse("Unable to authenticate! Your session may have expired. Please log out and log back in.");
        }

        UserDBData userData;
        try {
            userData = getUserDAO().getUser(request.getUsername());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("[DBError] Unable to find user \"" + request.getUsername() + "\": " + e.getMessage());
        }
        
        if (userData == null) {
            return new UserResponse("User \"" + request.getUsername() + "\" not found!");
        }
        
        return new UserResponse(userData.getUser());
    }

    public void putTestUsers() {
        List<UserDBData> userData = new ArrayList<>();
        String salt;
        String hashedPassword;
        int currentUserIndex = 0;
        try {
            salt = getSalt();
            hashedPassword = hashPassword(salt + "jdawg");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Unable to get salt");
        }

        try {
            getUserDAO().putUser("@jdawg", hashedPassword, salt, "Jake", "Hamzawi", "https://hamsesh-tweeter.s3.us-east-2.amazonaws.com/jdawg.png", 10000, 0);
        } catch (DAOException e) {
            throw new RuntimeException("Unable to batch put users. Failed at jdawg: " + e.getMessage());
        }

        for (int i = 0; i < 10000; i += 25) {
            userData.clear();
            for (int j = 0; j < 25; j++) {
                currentUserIndex = i + j;
                try {
                    salt = getSalt();
                    hashedPassword = hashPassword(salt + "test" + currentUserIndex);
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException("Unable to get salt");
                }


                userData.add(new UserDBData(new User(
                            "Test" + currentUserIndex,
                            "Test" + currentUserIndex,
                            "@test" + currentUserIndex,
                            "https://hamsesh-tweeter.s3.us-east-2.amazonaws.com/test.png"),
                        hashedPassword, salt));
            }

            try {
                getUserDAO().batchPutUsers(userData);
            } catch (DAOException e) {
                throw new RuntimeException("Unable to batch put users. Failed at " + i + "-" + (i+25) +
                        ": " + e.getMessage());
            }
        }
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
        String hashedClientPassword = hashPassword(salt + passwordFromClient);
        return passwordFromDB.equals(hashedClientPassword);
    }

    private AuthToken generateAuthToken() {
        return new AuthToken(UUID.randomUUID().toString(), generateDatetime());
    }
}
