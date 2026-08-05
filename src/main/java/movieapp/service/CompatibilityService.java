package movieapp.service;

import movieapp.db.FollowRepository;
import movieapp.db.MovieRepository;
import movieapp.db.ReviewRepository;
import movieapp.db.UserRepository;
import movieapp.exception.NotFoundException;
import movieapp.exception.NotMutualFollowException;
import movieapp.model.CompatibilityResult;
import movieapp.model.Follow;
import movieapp.model.Movie;
import movieapp.model.Review;
import movieapp.model.SharedMovieRating;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CompatibilityService {

    private final Connection connection;

    public CompatibilityService(Connection connection) {
        this.connection = connection;
    }

    public CompatibilityResult getCompatibility(int userId, int otherUserId) throws SQLException, NotFoundException, NotMutualFollowException {
        UserRepository userRepository = new UserRepository();

        // both users must actually exist
        if (userRepository.findById(connection, userId) == null)
            throw new NotFoundException("No user found with id: " + userId);
        if (userRepository.findById(connection, otherUserId) == null)
            throw new NotFoundException("No user found with id: " + otherUserId);

        // must be mutually following -- check both directions
        FollowRepository followRepository = new FollowRepository();
        boolean iFollowThem = isFollowing(followRepository.findFollowing(connection, userId), otherUserId);
        boolean theyFollowMe = isFollowing(followRepository.findFollowing(connection, otherUserId), userId);

        if (!iFollowThem || !theyFollowMe)
            throw new NotMutualFollowException("You must mutually follow this user to check compatibility");

        // gather both users' reviews
        ReviewRepository reviewRepository = new ReviewRepository();
        List<Review> myReviews = reviewRepository.findByUser(connection, userId);
        List<Review> theirReviews = reviewRepository.findByUser(connection, otherUserId);

        // find movies both reviewed, building the shared-movie comparison list
        MovieRepository movieRepository = new MovieRepository();
        List<SharedMovieRating> sharedMovies = new ArrayList<>();

        for (Review myReview : myReviews) {
            for (Review theirReview : theirReviews) {
                if (myReview.getMovieId() == theirReview.getMovieId()) {
                    Movie movie = movieRepository.findById(connection, myReview.getMovieId());
                    sharedMovies.add(new SharedMovieRating(
                            movie.getId(),
                            movie.getTitle(),
                            myReview.getRating(),
                            theirReview.getRating()
                    ));
                }
            }
        }

        // no overlap at all -- nothing to compute a score from
        if (sharedMovies.isEmpty())
            return new CompatibilityResult(null, sharedMovies);

        // average absolute difference across shared movies, converted to a 0-100% score
        // (max possible difference per movie is 9, since ratings are 1-10)
        double totalDifference = 0;
        for (SharedMovieRating shared : sharedMovies)
            totalDifference += Math.abs(shared.getMyRating() - shared.getTheirRating());

        double averageDifference = totalDifference / sharedMovies.size();
        double compatibilityScore = 100 - (averageDifference / 9 * 100);

        return new CompatibilityResult(compatibilityScore, sharedMovies);
    } // end of getCompatibility()

    // checks whether a list of Follow relationships contains one pointing at targetUserId
    private boolean isFollowing(List<Follow> followingList, int targetUserId) {
        for (Follow follow : followingList) {
            if (follow.getFolloweeId() == targetUserId)
                return true;
        }
        return false;
    } // end of isFollowing()

} // end of class