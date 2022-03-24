package edu.byu.cs.tweeter.server.dao.dynamo;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.dynamodbv2.document.DeleteItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PrimaryKey;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

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

public class UserDynamoDAO extends DynamoDAO implements UserDAO {
    private static final String USER_TABLE_NAME = "tweeter-users";
    private static final String BUCKET_NAME = "hamsesh-tweeter";

    private final AmazonS3 s3;

    public UserDynamoDAO() {
        super();
        s3 = AmazonS3ClientBuilder
                .standard()
                .withRegion(AWS_REGION)
                .build();
    }

    @Override
    public DBUserData getUser(String alias) throws DAOException {
        Item outcome;
        try {
            Table table = dynamoDB.getTable(USER_TABLE_NAME);
            GetItemSpec spec = new GetItemSpec().withPrimaryKey("alias", alias);

            outcome = table.getItem(spec);
        } catch (AmazonServiceException e) {
            throw new DAOException(e.getMessage());
        }

        if (outcome == null) {
            return null;
        }

        String firstName = outcome.getString("first_name");
        String lastName = outcome.getString("last_name");
        String imageURL = outcome.getString("image_url");
        String hashedDBPassword = outcome.getString("password");
        String salt = outcome.getString("salt");
        if (firstName == null ||
                lastName == null ||
                alias == null ||
                imageURL == null ||
                hashedDBPassword == null ||
                salt == null) {
            throw new DAOException("User improperly stored in db");
        }

        return new DBUserData(new User(firstName, lastName, alias, imageURL), hashedDBPassword, salt);
    }

    @Override
    public void putUser(String alias, String hashedPassword, String salt, String firstName,
                        String lastName, String imageURL, int numFollowers, int numFollowing)
            throws DAOException {
        try {
            Table userTable = dynamoDB.getTable(USER_TABLE_NAME);
            userTable.putItem(
                    new Item()
                            .withPrimaryKey("alias", alias)
                            .withString("password", hashedPassword + salt)
                            .withString("salt", salt)
                            .withString("first_name", firstName)
                            .withString("last_name", lastName)
                            .withString("image_url", imageURL)
                            .withInt("num_followers", numFollowers)
                            .withInt("num_following", numFollowing));
        } catch (AmazonServiceException e) {
            throw new DAOException(e.getMessage());
        }
    }

    @Override
    public void putAuthToken(AuthToken authToken) throws DAOException {
        try {
            Table table = dynamoDB.getTable(TOKEN_TABLE_NAME);
            table.putItem(
                    new Item()
                            .withPrimaryKey("auth_token", authToken.getToken())
                            .withString("datetime", authToken.getDatetime()));
        } catch (AmazonServiceException e) {
            throw new DAOException(e.getMessage());
        }
    }

    @Override
    public void deleteAuthToken(AuthToken authToken) throws DAOException {
        try {
            Table table = dynamoDB.getTable(TOKEN_TABLE_NAME);
            DeleteItemSpec deleteItemSpec = new DeleteItemSpec()
                    .withPrimaryKey(new PrimaryKey("auth_token", authToken.getToken()));
            table.deleteItem(deleteItemSpec);
        } catch (AmazonServiceException e) {
            throw new DAOException(e.getMessage());
        }
    }

    @Override
    public String uploadImage(byte[] image, String alias, ObjectMetadata metadata) throws DAOException {
        try {
            s3.putObject(new PutObjectRequest(
                    BUCKET_NAME,
                    alias + ".png",
                    new ByteArrayInputStream(image),
                    metadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead));
            return ((AmazonS3Client) s3).getResourceUrl(BUCKET_NAME, alias + ".png");
        } catch (SdkClientException e) {
            throw new DAOException(e.getMessage());
        }
    }

    @Override
    public boolean authenticate(AuthToken token) throws DAOException {
        Item outcome;
        try {
            Table table = dynamoDB.getTable(TOKEN_TABLE_NAME);
            GetItemSpec spec = new GetItemSpec().withPrimaryKey("auth_token", token.getToken());
            outcome = table.getItem(spec);
        } catch (AmazonServiceException e) {
            throw new DAOException(e.getMessage());
        }

        return outcome != null;
    }
}
