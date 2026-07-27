package movieapp.model;

import java.time.LocalDateTime;

public class Follow {

    private final int followerId;
    private final int followeeId;
    private final LocalDateTime createdAt;

    public Follow(int followerId, int followeeId, LocalDateTime createdAt) {
        this.followerId = followerId;
        this.followeeId = followeeId;
        this.createdAt = createdAt;
    }

    public int getFollowerId() {
        return followerId;
    }

    public int getFolloweeId() {
        return followeeId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

} // end of class