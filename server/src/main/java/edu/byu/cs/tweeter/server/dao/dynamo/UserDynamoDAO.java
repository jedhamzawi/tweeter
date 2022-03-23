package edu.byu.cs.tweeter.server.dao.dynamo;

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
import edu.byu.cs.tweeter.server.dao.UserDAO;

public class UserDynamoDAO extends DynamoDAO implements UserDAO {
    private static final String USER_TABLE_NAME = "tweeter-users";
    private static final String BUCKET_NAME = "hamsesh-tweeter";
    private static final String IMAGE_METADATA = "image/png";

    private final AmazonS3 s3;

    public UserDynamoDAO() {
        super();
        s3 = AmazonS3ClientBuilder
                .standard()
                .withRegion(AWS_REGION)
                .build();
    }

    @Override
    public LoginResponse login(LoginRequest request) throws DAOException {
        DBUserData userData = getUserFromDB(request.getUsername());
        if (userData == null) {
            return new LoginResponse("User not found");
        }

        try {
            if (!validatePassword(request.getPassword(), userData.hashedPassword, userData.salt)) {
                return new LoginResponse("Invalid login credentials! Try again");
            }
        } catch (NoSuchAlgorithmException e) {
            throw new DAOException("Unable to validate passwords: " + e.getMessage());
        }

        AuthToken authToken;
        try {
            authToken = putNewAuthToken();
        } catch (Exception e) {
            throw new DAOException("Unable to put authToken in table: " + e.getMessage());
        }

        return new LoginResponse(userData.user, authToken);
    }

    @Override
    public LogoutResponse logout(LogoutRequest request) throws DAOException {
        Table table = dynamoDB.getTable(TOKEN_TABLE_NAME);
        DeleteItemSpec deleteItemSpec = new DeleteItemSpec()
                .withPrimaryKey(new PrimaryKey("auth_token", request.getAuthToken().getToken()));

        try {
            System.out.println("Attempting a conditional delete...");
            table.deleteItem(deleteItemSpec);
            System.out.println("DeleteItem succeeded");
        }
        catch (Exception e) {
            throw new DAOException("Unable to delete authToken: " + e.getMessage());
        }
        return new LogoutResponse(true);
    }

    @Override
    public RegisterResponse register(RegisterRequest request) throws DAOException {
        if (getUserFromDB(request.getUsername()) != null) {
            return new RegisterResponse("User already exists!");
        }

        String hashedPassword;
        String salt;
        try {
            hashedPassword = hashPassword(request.getPassword());
            salt = getSalt();
        } catch (NoSuchAlgorithmException e) {
            throw new DAOException("Unable to hash password: " + e.getMessage());
        }

        String imageURL;
        try {
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentType(IMAGE_METADATA);
            s3.putObject(new PutObjectRequest(
                    BUCKET_NAME,
                    request.getUsername() + ".png",
                    new ByteArrayInputStream(request.getImage()),
                    objectMetadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead));
            imageURL = ((AmazonS3Client) s3).getResourceUrl(BUCKET_NAME, request.getUsername() + ".png");
        } catch (Exception e) {
            throw new DAOException("Unable to post image to S3 database: " + e.getMessage());
        }

        try {
            Table userTable = dynamoDB.getTable(USER_TABLE_NAME);
            PutItemOutcome putItemOutcome = userTable.putItem(
                    new Item()
                            .withPrimaryKey("alias", request.getUsername())
                            .withString("password", hashedPassword + salt)
                            .withString("salt", salt)
                            .withString("first_name", request.getFirstName())
                            .withString("last_name", request.getLastName())
                            .withString("image_url", imageURL)
                            .withInt("num_followers", 0)
                            .withInt("num_following", 0));
            System.out.println("Successfully put user in table: " + putItemOutcome.getPutItemResult());
        } catch (Exception e) {
            throw new DAOException("Unable to put user in table: " + e.getMessage());
        }

        AuthToken authToken;
        try {
            authToken = putNewAuthToken();
        } catch (Exception e) {
            throw new DAOException("Unable to put authToken in table: " + e.getMessage());
        }

        return new RegisterResponse(new User(request.getFirstName(), request.getLastName(), request.getUsername(), imageURL), authToken);
    }

    @Override
    public UserResponse getUser(UserRequest request) throws DAOException {
        if (!authenticate(request.getAuthToken())) {
            return new UserResponse("Unable to authenticate user. Try logging out and logging back in!");
        }

        DBUserData userData = getUserFromDB(request.getUsername());
        if (userData == null) {
            throw new DAOException("User \"" + request.getUsername() + "\" not found");
        }

        return new UserResponse(userData.user);
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

    private AuthToken putNewAuthToken() throws Exception {
        AuthToken authToken = generateAuthToken();
        Table table = dynamoDB.getTable(TOKEN_TABLE_NAME);
        PutItemOutcome putItemOutcome = table.putItem(
                    new Item()
                            .withPrimaryKey("auth_token", authToken.getToken())
                            .withString("datetime", authToken.getDatetime()));
        System.out.println("Successfully put authToken in table: " + putItemOutcome.getPutItemResult());
        return authToken;
    }

    private DBUserData getUserFromDB(String username) throws DAOException {
        Table table = dynamoDB.getTable(USER_TABLE_NAME);
        GetItemSpec spec = new GetItemSpec().withPrimaryKey("alias", username);

        Item outcome;
        try {
            outcome = table.getItem(spec);
        } catch (Exception e) {
            throw new DAOException("Unable to find user \"" + username + "\": " + e.getMessage());
        }

        if (outcome == null) {
            return null;
        }

        String firstName = outcome.getString("first_name");
        String lastName = outcome.getString("last_name");
        String alias = outcome.getString("alias");
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

    private static class DBUserData {
        private final User user;
        private final String hashedPassword;
        private final String salt;

        public DBUserData(User user, String hashedPassword, String salt) {
            this.user = user;
            this.hashedPassword = hashedPassword;
            this.salt = salt;
        }
    }
}
