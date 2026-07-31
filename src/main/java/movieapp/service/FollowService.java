package movieapp.service;

import movieapp.db.FollowRepository;
import movieapp.db.UserRepository;
import movieapp.exception.DuplicateFollowException;
import movieapp.exception.NotFoundException;
import movieapp.exception.SelfFollowException;
import movieapp.model.Follow;
import movieapp.model.User;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
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

    public List<User> getFollowing(int userId) throws SQLException, NotFoundException {
        UserRepository userRepository = new UserRepository();

        // check if user exists
        if (userRepository.findById(connection, userId) == null)
            throw new NotFoundException("No user found with id: " + userId);

        FollowRepository followRepository = new FollowRepository();
        List<Follow> followingList = followRepository.findFollowing(connection, userId);

        List<User> users = new ArrayList<>();
        for (Follow followRelationship : followingList)
            users.add(userRepository.findById(connection, followRelationship.getFolloweeId()));

        return users;
    } // end of getFollowing()

    public List<User> getFollowers(int userId) throws SQLException, NotFoundException {
        UserRepository userRepository = new UserRepository();

        // check if user exists
        if (userRepository.findById(connection, userId) == null)
            throw new NotFoundException("No user found with id: " + userId);

        FollowRepository followRepository = new FollowRepository();
        List<Follow> followersList = followRepository.findFollowers(connection, userId);

        List<User> users = new ArrayList<>();
        for (Follow followRelationship : followersList)
            users.add(userRepository.findById(connection, followRelationship.getFollowerId()));

        return users;
    } // end of getFollowers()

} // end of class