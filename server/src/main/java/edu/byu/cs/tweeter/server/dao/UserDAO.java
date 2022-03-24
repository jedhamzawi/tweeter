package edu.byu.cs.tweeter.server.dao;

import com.amazonaws.services.s3.model.ObjectMetadata;

import java.io.ByteArrayInputStream;
import java.util.List;

import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.User;

public interface UserDAO {
    DBUserData getUser(String alias) throws DAOException;
    List<User> batchGetUsers(List<String> aliases) throws DAOException;
    void putUser(String alias, String hashedPassword, String salt, String firstName,
                           String lastName, String imageURL, int numFollowers, int numFollowing)
            throws DAOException;
    void putAuthToken(AuthToken authToken) throws DAOException;
    void deleteAuthToken(AuthToken authToken) throws DAOException;
    String uploadImage(ByteArrayInputStream image, String alias, ObjectMetadata metadata) throws DAOException;
    boolean authenticate(AuthToken token) throws DAOException;
    int getFollowersCount(String alias) throws DAOException;
    int getFollowingCount(String alias) throws DAOException;
    void incrementFollowerCount(String alias, Integer val) throws DAOException;
    void incrementFollowingCount(String alias, Integer val) throws DAOException;
}
