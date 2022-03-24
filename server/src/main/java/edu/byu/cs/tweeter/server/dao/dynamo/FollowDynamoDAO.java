package edu.byu.cs.tweeter.server.dao.dynamo;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.dynamodbv2.document.Index;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.PrimaryKey;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.model.net.request.FollowRequest;
import edu.byu.cs.tweeter.model.net.request.GetFollowersCountRequest;
import edu.byu.cs.tweeter.model.net.request.GetFollowersRequest;
import edu.byu.cs.tweeter.model.net.request.GetFollowingCountRequest;
import edu.byu.cs.tweeter.model.net.request.GetFollowingRequest;
import edu.byu.cs.tweeter.model.net.request.IsFollowerRequest;
import edu.byu.cs.tweeter.model.net.request.UnfollowRequest;
import edu.byu.cs.tweeter.model.net.response.FollowResponse;
import edu.byu.cs.tweeter.model.net.response.GetFollowersCountResponse;
import edu.byu.cs.tweeter.model.net.response.GetFollowersResponse;
import edu.byu.cs.tweeter.model.net.response.GetFollowingCountResponse;
import edu.byu.cs.tweeter.model.net.response.GetFollowingResponse;
import edu.byu.cs.tweeter.model.net.response.IsFollowerResponse;
import edu.byu.cs.tweeter.model.net.response.UnfollowResponse;
import edu.byu.cs.tweeter.server.dao.DAOException;
import edu.byu.cs.tweeter.server.dao.FollowDAO;
import edu.byu.cs.tweeter.util.FakeData;

/**
 * A DAO for accessing 'following' data from the database.
 */
public class FollowDynamoDAO extends DynamoDAO implements FollowDAO {
    private static final String FOLLOW_TABLE_NAME = "tweeter_follows";
    private static final String INDEX_NAME = "follower_followee_index";
    private static final String FOLLOWING_KEY = "followee_alias";
    private static final String FOLLOWER_KEY = "follower_alias";

    public FollowDynamoDAO() {
        super();
    }

    public List<String> getFollowers(String targetAlias, int limit, String lastUserAlias) throws DAOException {
        QuerySpec querySpec = new QuerySpec().withKeyConditionExpression(FOLLOWING_KEY + " = :fh")
                .withValueMap(new ValueMap()
                    .withString(":fh", targetAlias))
                .withScanIndexForward(true)
                .withMaxResultSize(limit);
        if (lastUserAlias != null) {
            querySpec.withExclusiveStartKey(new PrimaryKey(FOLLOWER_KEY, lastUserAlias));
        }

        try {
            Table table = dynamoDB.getTable(FOLLOW_TABLE_NAME);
            ItemCollection<QueryOutcome> items = table.query(querySpec);
            if (items.getLastLowLevelResult().getQueryResult().getLastEvaluatedKey() == null) {
                return null;
            }
            else {
                return convertResultsToStrings(items, FOLLOWER_KEY);
            }
        } catch (AmazonServiceException e) {
            throw new DAOException(e.getMessage());
        }
    }

    @Override
    public List<String> getFollowees(String targetAlias, int limit, String lastUserAlias) throws DAOException {
        QuerySpec querySpec = new QuerySpec().withKeyConditionExpression(FOLLOWER_KEY + " = :fh")
                .withValueMap(new ValueMap()
                        .withString(":fh", targetAlias))
                .withScanIndexForward(true)
                .withMaxResultSize(limit);
        if (lastUserAlias != null) {
            querySpec.withExclusiveStartKey(new PrimaryKey(FOLLOWING_KEY, lastUserAlias));
        }

        try {
            Table table = dynamoDB.getTable(FOLLOW_TABLE_NAME);
            Index index = table.getIndex(INDEX_NAME);
            ItemCollection<QueryOutcome> items = index.query(querySpec);
            if (items.getLastLowLevelResult().getQueryResult().getLastEvaluatedKey() == null) {
                return null;
            }
            else {
                return convertResultsToStrings(items, FOLLOWING_KEY);
            }
        } catch (AmazonServiceException e) {
            throw new DAOException(e.getMessage());
        }
    }

    @Override
    public void putFollower(String followeeAlias, String followerAlias) throws DAOException {
        try {
            Table table = dynamoDB.getTable(FOLLOW_TABLE_NAME);
            table.putItem(new Item()
                    .withPrimaryKey(FOLLOWING_KEY, followeeAlias, FOLLOWER_KEY, followerAlias));
        } catch (AmazonServiceException e) {
            throw new DAOException(e.getMessage());
        }
    }

    @Override
    public void deleteFollower(String followeeAlias, String followerAlias) throws DAOException {
        try {
            Table table = dynamoDB.getTable(FOLLOW_TABLE_NAME);
            DeleteItemSpec deleteItemSpec = new DeleteItemSpec()
                    .withPrimaryKey(new PrimaryKey(FOLLOWING_KEY, followeeAlias, FOLLOWER_KEY, followerAlias));
            table.deleteItem(deleteItemSpec);
        } catch (AmazonServiceException e) {
            throw new DAOException(e.getMessage());
        }
    }

    @Override
    public boolean isFollower(String loggedInUserAlias, String targetUserAllias) throws DAOException {
        GetItemSpec spec = new GetItemSpec().withPrimaryKey(FOLLOWING_KEY, loggedInUserAlias, FOLLOWER_KEY, targetUserAllias);
        try {
            Table table = dynamoDB.getTable(FOLLOW_TABLE_NAME);
            Item outcome = table.getItem(spec);
            return outcome != null;
        } catch (AmazonServiceException e) {
            throw new DAOException(e.getMessage());
        }
    }

    private List<String> convertResultsToStrings(ItemCollection<QueryOutcome> results, String key) {
        Iterator<Item> iterator = results.iterator();
        Item item = null;
        List<String> strings = new ArrayList<>();
        while (iterator.hasNext()) {
            item = iterator.next();
            strings.add(item.getString(key));
        }
        return strings;
    }
}
