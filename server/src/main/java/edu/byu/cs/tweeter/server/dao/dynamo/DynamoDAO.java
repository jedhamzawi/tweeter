package edu.byu.cs.tweeter.server.dao.dynamo;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.server.dao.DAOException;

public class DynamoDAO {
    protected static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    protected static final String AWS_REGION = "us-east-2";
    protected static final String TOKEN_TABLE_NAME = "tweeter-tokens";
    protected final AmazonDynamoDB client;
    protected final DynamoDB dynamoDB;

    public DynamoDAO() {
        client = AmazonDynamoDBClientBuilder
                .standard()
                .withRegion(AWS_REGION)
                .build();
        dynamoDB = new DynamoDB(client);
    }

    protected boolean authenticate(AuthToken token) throws DAOException {
        try {
            Table table = dynamoDB.getTable(TOKEN_TABLE_NAME);
            GetItemSpec spec = new GetItemSpec().withPrimaryKey("auth_token", token.getToken());
            Item outcome = table.getItem(spec);

            return outcome != null;
        } catch (Exception e) {
            throw new DAOException("Unable to authenticate logged in user: " + e.getMessage());
        }
    }
}
