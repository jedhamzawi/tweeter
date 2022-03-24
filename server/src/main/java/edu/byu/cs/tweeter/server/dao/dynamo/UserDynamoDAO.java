package edu.byu.cs.tweeter.server.dao.dynamo;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.dynamodbv2.document.BatchGetItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PrimaryKey;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.TableKeysAndAttributes;
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.server.dao.DAOException;
import edu.byu.cs.tweeter.server.dao.DBUserData;
import edu.byu.cs.tweeter.server.dao.UserDAO;

public class UserDynamoDAO extends DynamoDAO implements UserDAO {
    private static final String USER_TABLE_NAME = "tweeter-users";
    private static final String BUCKET_NAME = "hamsesh-tweeter";
    private static final String USER_KEY = "alias";

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
            GetItemSpec spec = new GetItemSpec().withPrimaryKey(USER_KEY, alias);

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
    public List<User> batchGetUsers(List<String> aliases) throws DAOException {
        TableKeysAndAttributes userTableKeysAndAttributes = new TableKeysAndAttributes(USER_TABLE_NAME);
        userTableKeysAndAttributes.addHashOnlyPrimaryKey(USER_KEY, aliases.toArray(new String[0]));

        try {
            BatchGetItemOutcome outcome = dynamoDB.batchGetItem(userTableKeysAndAttributes);
            return convertItemsToUsers(outcome.getTableItems().get(USER_TABLE_NAME));
        } catch (AmazonServiceException e) {
            throw new DAOException(e.getMessage());
        }
    }

    @Override
    public void putUser(String alias, String hashedPassword, String salt, String firstName,
                        String lastName, String imageURL, int numFollowers, int numFollowing)
            throws DAOException {
        try {
            Table userTable = dynamoDB.getTable(USER_TABLE_NAME);
            userTable.putItem(
                    new Item()
                            .withPrimaryKey(USER_KEY, alias)
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
    public String uploadImage(ByteArrayInputStream image, String alias, ObjectMetadata metadata) throws DAOException {
        try {
            s3.putObject(new PutObjectRequest(BUCKET_NAME, alias + ".png", image, metadata)
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

    @Override
    public int getFollowersCount(String alias) throws DAOException {
        Item outcome;
        try {
            Table table = dynamoDB.getTable(USER_TABLE_NAME);
            GetItemSpec spec = new GetItemSpec().withPrimaryKey(USER_KEY, alias);

            outcome = table.getItem(spec);
        } catch (AmazonServiceException e) {
            throw new DAOException(e.getMessage());
        }

        if (outcome == null) {
            return -1;
        }

        try {
            return outcome.getInt("num_followers");
        } catch (NumberFormatException e) {
            throw new DAOException(e.getMessage());
        }
    }

    @Override
    public int getFollowingCount(String alias) throws DAOException {
        Item outcome;
        try {
            Table table = dynamoDB.getTable(USER_TABLE_NAME);
            GetItemSpec spec = new GetItemSpec().withPrimaryKey(USER_KEY, alias);

            outcome = table.getItem(spec);
        } catch (AmazonServiceException e) {
            throw new DAOException(e.getMessage());
        }

        if (outcome == null) {
            return -1;
        }

        try {
            return outcome.getInt("num_following");
        } catch (NumberFormatException e) {
            throw new DAOException(e.getMessage());
        }
    }

    @Override
    public void incrementFollowerCount(String alias, Integer val) throws DAOException {
        try {
            Table table = dynamoDB.getTable(USER_TABLE_NAME);

            Map<String, String> expressionAttributeNames = new HashMap<>();
            expressionAttributeNames.put("#f", "num_followers");

            Map<String, Object> expressionAttributeValues = new HashMap<>();
            expressionAttributeValues.put(":val", val);

            table.updateItem(USER_KEY, alias,
                    "set #f = #f + :val",
                    expressionAttributeNames,
                    expressionAttributeValues);
        } catch (AmazonServiceException e) {
            throw new DAOException(e.getMessage());
        }
    }

    @Override
    public void incrementFollowingCount(String alias, Integer val) throws DAOException {
        try {
            Table table = dynamoDB.getTable(USER_TABLE_NAME);

            Map<String, String> expressionAttributeNames = new HashMap<>();
            expressionAttributeNames.put("#f", "num_following");

            Map<String, Object> expressionAttributeValues = new HashMap<>();
            expressionAttributeValues.put(":val", val);

            table.updateItem(USER_KEY, alias,
                    "set #f = #f + :val",
                    expressionAttributeNames,
                    expressionAttributeValues);
        } catch (AmazonServiceException e) {
            throw new DAOException(e.getMessage());
        }
    }

    private List<User> convertItemsToUsers(List<Item> items) {
        List<User> users = new ArrayList<>();
        for (Item item : items) {
            String firstName = item.getString("first_name");
            String lastName = item.getString("lastName");
            String alias = item.getString(USER_KEY);
            String imageURL = item.getString("image_url");
            users.add(new User(firstName, lastName, alias, imageURL));
        }
        return users;
    }
}
