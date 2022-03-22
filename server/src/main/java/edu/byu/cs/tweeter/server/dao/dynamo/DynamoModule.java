package edu.byu.cs.tweeter.server.dao.dynamo;

import com.google.inject.AbstractModule;

import edu.byu.cs.tweeter.server.dao.FollowDAO;
import edu.byu.cs.tweeter.server.dao.StatusDAO;

public class DynamoModule extends AbstractModule {
    @Override
    public void configure() {
        bind(FollowDAO.class).to(FollowDynamoDAO.class);
        bind(StatusDAO.class).to(StatusDynamoDAO.class);
    }
}
