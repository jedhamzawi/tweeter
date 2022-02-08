package edu.byu.cs.tweeter.client.presenter;

import java.util.List;

import edu.byu.cs.tweeter.client.model.service.observer.PagedServiceObserver;
import edu.byu.cs.tweeter.client.presenter.view.PagedView;
import edu.byu.cs.tweeter.model.domain.User;

public abstract class PagedPresenter<T> extends Presenter {
    protected static final int PAGE_SIZE = 10;

    protected final PagedView<T> view;
    protected boolean hasMorePages;
    protected boolean isLoading = false;
    protected T lastItem;

    public PagedPresenter(PagedView<T> view) {
        super(view);
        this.view = view;
    }

    public boolean hasMorePages() {
        return hasMorePages;
    }

    public void setHasMorePages(boolean hasMorePages) {
        this.hasMorePages = hasMorePages;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public void loadMoreItems(User user) {
        if (!isLoading) {
            isLoading = true;
            view.setLoadingStatus(true);
            getItems(user);
        }
    }

    protected abstract void getItems(User user);

    protected abstract class PagedObserver implements PagedServiceObserver<T> {

        @Override
        public void handleSuccess(List<T> items, boolean hasMorePages) {
            isLoading = false;
            view.setLoadingStatus(false);
            setHasMorePages(hasMorePages);
            lastItem = (items.size() > 0) ? items.get(items.size() - 1) : null;
            view.addItems(items);
        }

        @Override
        public void handleFailure(String message) {
            isLoading = false;
            view.setLoadingStatus(false);
            displayErrorMessage(message);
        }

        @Override
        public void handleException(Exception ex) {
            isLoading = false;
            view.setLoadingStatus(false);
            displayExceptionMessage(ex);
        }

        protected abstract void displayErrorMessage(String message);
        protected abstract void displayExceptionMessage(Exception ex);
    }
}
