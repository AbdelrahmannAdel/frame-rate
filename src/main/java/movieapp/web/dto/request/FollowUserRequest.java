package movieapp.web.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FollowUserRequest {

    private int followeeId;

    public FollowUserRequest() {
    }

    public int getFolloweeId() {
        return followeeId;
    }

    public void setFolloweeId(int followeeId) {
        this.followeeId = followeeId;
    }

} // end of class