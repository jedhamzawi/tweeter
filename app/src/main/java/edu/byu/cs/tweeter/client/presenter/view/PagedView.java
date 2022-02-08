package edu.byu.cs.tweeter.client.presenter.view;

import java.util.List;

import edu.byu.cs.tweeter.model.domain.User;

public interface PagedView<T> extends View {
    void setLoadingStatus(boolean value);
    void addItems(List<T> items);
    void newUserActivity(User user);
}
