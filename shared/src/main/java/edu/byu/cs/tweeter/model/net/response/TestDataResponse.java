package edu.byu.cs.tweeter.model.net.response;

public class TestDataResponse extends Response {
    public TestDataResponse(String message) {
        super(message);
    }

    public TestDataResponse(boolean success) {
        super(success);
    }
}
