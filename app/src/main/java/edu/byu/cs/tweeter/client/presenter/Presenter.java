package edu.byu.cs.tweeter.client.presenter;

import edu.byu.cs.tweeter.client.presenter.view.View;

public class Presenter {
    protected final View view;

    public Presenter(View view) { this.view = view; }
}
