package edu.byu.cs.tweeter.client.model.service;

import android.os.Message;

import edu.byu.cs.tweeter.client.model.service.backgroundTask.FollowTask;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.GetFollowersCountTask;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.GetFollowersTask;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.GetFollowingCountTask;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.GetFollowingTask;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.IsFollowerTask;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.UnfollowTask;
import edu.byu.cs.tweeter.client.model.service.handler.PagedServiceHandler;
import edu.byu.cs.tweeter.client.model.service.handler.ServiceHandler;
import edu.byu.cs.tweeter.client.model.service.observer.PagedServiceObserver;
import edu.byu.cs.tweeter.client.model.service.observer.ServiceObserver;
import edu.byu.cs.tweeter.client.presenter.MainPresenter;
import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.User;

public class FollowService extends Service {

    public interface GetFollowingObserver extends PagedServiceObserver<User> {}
    public interface GetFollowersObserver extends PagedServiceObserver<User> {}
    public interface IsFollowerObserver extends ServiceObserver { void handleSuccess(boolean isFollower); }
    public interface FollowObserver extends ServiceObserver { void handleSuccess(); }
    public interface UnfollowObserver extends ServiceObserver { void handleSuccess(); }
    public interface GetFollowersCountObserver extends ServiceObserver { void handleSuccess(int count); }
    public interface GetFollowingCountObserver extends ServiceObserver { void handleSuccess(int count); }

    public void getFollowing(AuthToken currUserAuthToken, User user, int pageSize, User lastFollowee,
                             GetFollowingObserver getFollowingObserver) {
        executeTask(new GetFollowingTask(currUserAuthToken, user, pageSize, lastFollowee, new GetFollowingHandler(getFollowingObserver)));
    }

    public void getFollowers(AuthToken currUserAuthToken, User user, int pageSize, User lastFollower,
                             FollowService.GetFollowersObserver getFollowersObserver) {
        executeTask(new GetFollowersTask(currUserAuthToken, user, pageSize, lastFollower, new GetFollowersHandler(getFollowersObserver)));
    }

    public void isFollower(AuthToken token, User currUser, User selectedUser, MainPresenter.IsFollowerObserver isFollowerObserver) {
        executeTask(new IsFollowerTask(token, currUser, selectedUser, new IsFollowerHandler(isFollowerObserver)));
    }

    public void follow(AuthToken currUserAuthToken, User selectedUser, MainPresenter.FollowObserver followObserver) {
        executeTask(new FollowTask(currUserAuthToken, selectedUser, new FollowHandler(followObserver)));
    }

    public void unfollow(AuthToken currUserAuthToken, User selectedUser, MainPresenter.UnfollowObserver unfollowObserver) {
        executeTask(new UnfollowTask(currUserAuthToken, selectedUser, new UnfollowHandler(unfollowObserver)));
    }

    public void getFollowersCount(AuthToken currUserAuthToken, User selectedUser, MainPresenter.GetFollowersCountObserver observer) {
        executeTask(new GetFollowersCountTask(currUserAuthToken, selectedUser, new GetFollowersCountHandler(observer)));
    }

    public void getFollowingCount(AuthToken currUserAuthToken, User selectedUser, MainPresenter.GetFollowingCountObserver observer) {
        executeTask(new GetFollowingCountTask(currUserAuthToken, selectedUser, new GetFollowingCountHandler(observer)));
    }

    /**
     * Message handler (i.e., observer) for GetFollowingTask.
     */
    private class GetFollowingHandler extends PagedServiceHandler<User> {
        public GetFollowingHandler(GetFollowingObserver observer) { super(observer); }
    }

    /**
     * Message handler (i.e., observer) for GetFollowersTask.
     */
    private class GetFollowersHandler extends PagedServiceHandler<User> {
        public GetFollowersHandler(GetFollowersObserver observer) { super(observer); }
    }

    private class IsFollowerHandler extends ServiceHandler {
        private final IsFollowerObserver observer;

        public IsFollowerHandler(IsFollowerObserver observer) {
            this.observer = observer;
        }

        @Override
        public void handleSuccess(Message msg) {
            boolean isFollower = msg.getData().getBoolean(IsFollowerTask.IS_FOLLOWER_KEY);
            observer.handleSuccess(isFollower);
        }
        @Override
        public void handleFailure(String message) {
            observer.handleFailure(message);
        }
        @Override
        public void handleException(Exception ex) {
            observer.handleException(ex);
        }
    }

    private class FollowHandler extends ServiceHandler {
        private final FollowObserver observer;

        public FollowHandler(FollowObserver observer) {
            this.observer = observer;
        }

        @Override
        public void handleSuccess(Message msg) {
            observer.handleSuccess();
        }
        @Override
        public void handleFailure(String message) {
            observer.handleFailure(message);
        }
        @Override
        public void handleException(Exception ex) {
            observer.handleException(ex);
        }
    }

    private class UnfollowHandler extends ServiceHandler {
        private final UnfollowObserver observer;

        public UnfollowHandler(UnfollowObserver observer) {
            this.observer = observer;
        }

        @Override
        public void handleSuccess(Message msg) {
            observer.handleSuccess();
        }
        @Override
        public void handleFailure(String message) {
            observer.handleFailure(message);
        }
        @Override
        public void handleException(Exception ex) {
            observer.handleException(ex);
        }
    }

    private class GetFollowersCountHandler extends ServiceHandler {
        private final GetFollowersCountObserver observer;

        public GetFollowersCountHandler(GetFollowersCountObserver observer) {
            this.observer = observer;
        }

        @Override
        public void handleSuccess(Message msg) {
            int count = msg.getData().getInt(GetFollowersCountTask.COUNT_KEY);
            observer.handleSuccess(count);
        }
        @Override
        public void handleFailure(String message) {
            observer.handleFailure(message);
        }
        @Override
        public void handleException(Exception ex) {
            observer.handleException(ex);
        }
    }

    private class GetFollowingCountHandler extends ServiceHandler {
        private final GetFollowingCountObserver observer;

        public GetFollowingCountHandler(GetFollowingCountObserver observer) {
            this.observer = observer;
        }

        @Override
        public void handleSuccess(Message msg) {
            int count = msg.getData().getInt(GetFollowingCountTask.COUNT_KEY);
            observer.handleSuccess(count);
        }
        @Override
        public void handleFailure(String message) {
            observer.handleFailure(message);
        }
        @Override
        public void handleException(Exception ex) {
            observer.handleException(ex);
        }
    }
}
