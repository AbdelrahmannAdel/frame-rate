package movieapp.model;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class FollowId implements Serializable {

    private Integer followerId;
    private Integer followeeId;

    public FollowId() {
    }

    public FollowId(Integer followerId, Integer followeeId) {
        this.followerId = followerId;
        this.followeeId = followeeId;
    }

    public Integer getFollowerId() {
        return followerId;
    }

    public void setFollowerId(Integer followerId) {
        this.followerId = followerId;
    }

    public Integer getFolloweeId() {
        return followeeId;
    }

    public void setFolloweeId(Integer followeeId) {
        this.followeeId = followeeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        FollowId that = (FollowId) o;
        return Objects.equals(followerId, that.followerId) && Objects.equals(followeeId, that.followeeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(followerId, followeeId);
    }

} // end of class