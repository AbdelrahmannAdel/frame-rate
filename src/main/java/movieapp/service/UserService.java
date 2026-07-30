package movieapp.service;

import movieapp.db.FollowRepository;
import movieapp.db.ReviewRepository;
import movieapp.db.UserRepository;
import movieapp.db.WatchlistRepository;
import movieapp.exception.DuplicateEmailException;
import movieapp.exception.DuplicateUsernameException;
import movieapp.exception.NotFoundException;
import movieapp.model.Follow;
import movieapp.model.Review;
import movieapp.model.User;
import movieapp.model.WatchlistEntry;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class UserService {

    private final Connection connection;

    public UserService(Connection connection){
        this.connection = connection;
    }

    public User registerUser(String username, String email, String passwordHash) throws SQLException, DuplicateUsernameException, DuplicateEmailException {
        UserRepository userRepository = new UserRepository();

        User existingUserByUsername = userRepository.findByUsername(connection, username);
        if (existingUserByUsername != null)
            throw new DuplicateUsernameException("Username already taken: " + username);

        User existingUserByEmail = userRepository.findByEmail(connection, email);
        if (existingUserByEmail != null)
            throw new DuplicateEmailException("Email already taken: " + email);

        return userRepository.create(connection,username,email,passwordHash);
    } // end of registerUser()

    public boolean deleteUser(int userId) throws SQLException, NotFoundException {
        UserRepository userRepository = new UserRepository();

        ReviewRepository reviewRepository = new ReviewRepository();
        WatchlistRepository watchlistRepository = new WatchlistRepository();
        FollowRepository followRepository = new FollowRepository();

        User user = userRepository.findById(connection, userId);

        // check if user exists
        if (user == null)
            throw new NotFoundException("No user found with id: " + userId);

        // delete all user's reviews
        List<Review> reviewsList = reviewRepository.findByUser(connection, userId);
        for (Review review: reviewsList)
            reviewRepository.delete(connection,review.getId());

        // delete user's watchlist
        List<WatchlistEntry> watchList = watchlistRepository.findByUser(connection,userId);
        for (WatchlistEntry watchlistEntry: watchList)
            watchlistRepository.remove(connection,watchlistEntry.getId());

        // delete user's follow relationships:
        // 1 - remove follow relationships where this user is the follower
        List<Follow> followingsList = followRepository.findFollowing(connection, userId);
        for (Follow follow : followingsList)
            followRepository.unfollow(connection, userId, follow.getFolloweeId());

        // 2 - remove follow relationships where this user is being followed
        List<Follow> followersList = followRepository.findFollowers(connection, userId);
        for (Follow follow : followersList)
            followRepository.unfollow(connection, follow.getFollowerId(), userId);

        return userRepository.delete(connection,userId);
    } // end of deleteUser()

} // end of class