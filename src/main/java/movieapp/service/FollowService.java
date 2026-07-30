package movieapp.service;

import movieapp.db.FollowRepository;
import movieapp.exception.DuplicateFollowException;
import movieapp.exception.NotFoundException;
import movieapp.exception.SelfFollowException;
import movieapp.model.Follow;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class FollowService {

    private final Connection connection;
    public FollowService(Connection connection) {
        this.connection = connection;
    }

    public Follow followUser(int followerId, int followeeId) throws SQLException, SelfFollowException, DuplicateFollowException {
        FollowRepository followRepository = new FollowRepository();

        if (followerId == followeeId)
            throw new SelfFollowException("User " + followerId + " cannot follow themselves");

        List<Follow> followingList = followRepository.findFollowing(connection,followerId);
        for (Follow follow: followingList){
            if (follow.getFolloweeId() == followeeId)
                throw new DuplicateFollowException("User " + followerId + " is already following user " + followeeId);
        }

        return followRepository.follow(connection, followerId, followeeId);
    } // end of followUser()

    public boolean unfollowUser(int followerId, int followeeId) throws SQLException, NotFoundException {
        FollowRepository followRepository = new FollowRepository();

        List<Follow> followingList = followRepository.findFollowing(connection,followerId);
        boolean flag = false;
        for (Follow follow: followingList){
            if (follow.getFolloweeId() == followeeId){
                flag = true;
                break;
            }
        }

        if (!flag)
            throw new NotFoundException("User " + followerId + " is not following user " + followeeId);

         return followRepository.unfollow(connection, followerId, followeeId);
    } // end of unfollowUser()

} // end of class