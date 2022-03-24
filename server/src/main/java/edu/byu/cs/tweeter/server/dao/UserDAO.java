package edu.byu.cs.tweeter.server.dao;

import com.amazonaws.services.s3.model.ObjectMetadata;

import edu.byu.cs.tweeter.model.domain.AuthToken;

public interface UserDAO {
    DBUserData getUser(String alias) throws DAOException;
    void putUser(String alias, String hashedPassword, String salt, String firstName,
                           String lastName, String imageURL, int numFollowers, int numFollowing)
            throws DAOException;
    void putAuthToken(AuthToken authToken) throws DAOException;
    void deleteAuthToken(AuthToken authToken) throws DAOException;
    String uploadImage(byte[] image, String alias, ObjectMetadata metadata) throws DAOException;
    boolean authenticate(AuthToken token) throws DAOException;
}
