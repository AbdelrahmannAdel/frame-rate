package movieapp.repository;

import movieapp.model.Follow;
import movieapp.model.FollowId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FollowRepository extends JpaRepository<Follow, FollowId> {

    List<Follow> findByFollower_Id(int followerId);

    List<Follow> findByFollowee_Id(int followeeId);

} // end of interface