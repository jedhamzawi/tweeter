package edu.byu.cs.tweeter.model.net.response;

import java.util.List;

import edu.byu.cs.tweeter.model.domain.Status;

public class GetStoryResponse extends PagedResponse<Status> {
    public GetStoryResponse(boolean success, boolean hasMorePages) {
        super(success, hasMorePages);
    }

    public GetStoryResponse(boolean success, String message, boolean hasMorePages) {
        super(success, message, hasMorePages);
    }

    public GetStoryResponse(List<Status> items, boolean hasMorePages) {
        super(items, hasMorePages);
    }
}
