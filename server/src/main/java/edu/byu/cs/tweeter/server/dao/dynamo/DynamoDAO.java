package edu.byu.cs.tweeter.server.dao.dynamo;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class DynamoDAO {
    protected static final String AWS_REGION = "us-east-2";
    protected static final String TOKEN_TABLE_NAME = "tweeter-tokens";
    protected static final int BASE_TIMEOUT = 5;

    protected final AmazonDynamoDB client;
    protected final DynamoDB dynamoDB;

    public DynamoDAO() {
        client = AmazonDynamoDBClientBuilder
                .standard()
                .withRegion(AWS_REGION)
                .build();
        dynamoDB = new DynamoDB(client);
    }

    protected void expWait(double retries) {
        try {
            TimeUnit.MILLISECONDS.sleep((long) (BASE_TIMEOUT * (Math.pow(2, retries))));
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    protected List<String> convertResultsToStrings(ItemCollection<QueryOutcome> results, String hashKey) {
        Iterator<Item> iterator = results.iterator();
        Item item = null;
        List<String> strings = new ArrayList<>();
        while (iterator.hasNext()) {
            item = iterator.next();
            strings.add(item.getString(hashKey));
        }
        return strings;
    }

    protected List<String> convertResultsToStrings(ItemCollection<QueryOutcome> results, String hashKey, String sortKey) {
        Iterator<Item> iterator = results.iterator();
        Item item;
        List<String> strings = new ArrayList<>();
        while (iterator.hasNext()) {
            item = iterator.next();
            strings.add(item.getString(hashKey));
            strings.add(item.getString(sortKey));
        }
        return strings;
    }
}
