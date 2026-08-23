package movieapp.web.dto.response;

import movieapp.model.Follow;

import java.time.LocalDateTime;

public class FollowResponse {

    private final Integer followerId;
    private final Integer followeeId;
    private final LocalDateTime createdAt;

    public FollowResponse(Follow follow) {
        this.followerId = follow.getFollower().getId();
        this.followeeId = follow.getFollowee().getId();
        this.createdAt = follow.getCreatedAt();
    }

    public Integer getFollowerId() { return followerId; }
    public Integer getFolloweeId() { return followeeId; }
    public LocalDateTime getCreatedAt() { return createdAt; }

} // end of class