package movieapp.service;

import movieapp.exception.NotFoundException;
import movieapp.exception.NotMutualFollowException;
import movieapp.model.*;
import movieapp.repository.FollowRepository;
import movieapp.repository.ReviewRepository;
import movieapp.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class CompatibilityService {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final ReviewRepository reviewRepository;

    public CompatibilityService(UserRepository userRepository,
                                FollowRepository followRepository,
                                ReviewRepository reviewRepository) {
        this.userRepository = userRepository;
        this.followRepository = followRepository;
        this.reviewRepository = reviewRepository;
    }

    @Transactional(readOnly = true)
    public CompatibilityResult getCompatibility(int userId, int otherUserId)
            throws NotFoundException, NotMutualFollowException {

        if (userRepository.findById(userId).isEmpty())
            throw new NotFoundException("No user found with id: " + userId);
        if (userRepository.findById(otherUserId).isEmpty())
            throw new NotFoundException("No user found with id: " + otherUserId);

        boolean iFollowThem = followRepository.existsById(new FollowId(userId, otherUserId));
        boolean theyFollowMe = followRepository.existsById(new FollowId(otherUserId, userId));

        if (!iFollowThem || !theyFollowMe)
            throw new NotMutualFollowException("You must mutually follow this user to check compatibility");

        List<Review> myReviews = reviewRepository.findByUser_Id(userId);
        List<Review> theirReviews = reviewRepository.findByUser_Id(otherUserId);

        List<SharedMovieRating> sharedMovies = new ArrayList<>();
        for (Review myReview : myReviews) {
            for (Review theirReview : theirReviews) {
                if (myReview.getMovie().getId().equals(theirReview.getMovie().getId())) {
                    Movie movie = myReview.getMovie();
                    sharedMovies.add(new SharedMovieRating(
                            movie.getId(),
                            movie.getTitle(),
                            myReview.getRating(),
                            theirReview.getRating()
                    ));
                }
            }
        }

        if (sharedMovies.isEmpty())
            return new CompatibilityResult(null, sharedMovies);

        double totalDifference = 0;
        for (SharedMovieRating shared : sharedMovies)
            totalDifference += Math.abs(shared.getMyRating() - shared.getTheirRating());

        double averageDifference = totalDifference / sharedMovies.size();
        double compatibilityScore = 100 - (averageDifference / 9 * 100);

        return new CompatibilityResult(compatibilityScore, sharedMovies);
    } // end of getCompatibility()

} // end of class