package movieapp.service;

import movieapp.exception.*;
import movieapp.model.Follow;
import movieapp.model.FollowId;
import movieapp.model.User;
import movieapp.repository.FollowRepository;
import movieapp.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    public FollowService(FollowRepository followRepository, UserRepository userRepository) {
        this.followRepository = followRepository;
        this.userRepository = userRepository;
    }

    public Follow followUser(int followerId, int followeeId)
            throws NotFoundException, SelfFollowException, DuplicateFollowException {

        if (followerId == followeeId)
            throw new SelfFollowException("User " + followerId + " cannot follow themselves");

        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new NotFoundException("No user found with id: " + followerId));
        User followee = userRepository.findById(followeeId)
                .orElseThrow(() -> new NotFoundException("No user found with id: " + followeeId));

        if (followRepository.existsById(new FollowId(followerId, followeeId)))
            throw new DuplicateFollowException("User " + followerId + " is already following user " + followeeId);

        return followRepository.save(new Follow(follower, followee));
    } // end of followUser()

    public boolean unfollowUser(int followerId, int followeeId) throws NotFoundException {
        FollowId id = new FollowId(followerId, followeeId);

        if (!followRepository.existsById(id))
            throw new NotFoundException("User " + followerId + " is not following user " + followeeId);

        followRepository.deleteById(id);
        return true;
    } // end of unfollowUser()

    public List<User> getFollowing(int userId) throws NotFoundException {
        if (userRepository.findById(userId).isEmpty())
            throw new NotFoundException("No user found with id: " + userId);

        List<User> users = new ArrayList<>();
        for (Follow follow : followRepository.findByFollower_Id(userId))
            users.add(follow.getFollowee());
        return users;
    } // end of getFollowing()

    public List<User> getFollowers(int userId) throws NotFoundException {
        if (userRepository.findById(userId).isEmpty())
            throw new NotFoundException("No user found with id: " + userId);

        List<User> users = new ArrayList<>();
        for (Follow follow : followRepository.findByFollowee_Id(userId))
            users.add(follow.getFollower());
        return users;
    } // end of getFollowers()

} // end of class