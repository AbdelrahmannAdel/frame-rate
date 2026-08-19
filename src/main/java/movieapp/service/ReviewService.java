package movieapp.service;

import movieapp.exception.*;
import movieapp.model.Movie;
import movieapp.model.Review;
import movieapp.model.User;
import movieapp.repository.MovieRepository;
import movieapp.repository.ReviewRepository;
import movieapp.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;

    public ReviewService(ReviewRepository reviewRepository,
                         UserRepository userRepository,
                         MovieRepository movieRepository) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.movieRepository = movieRepository;
    }

    public Review createReview(int userId, int movieId, int rating)
            throws NotFoundException, InvalidRatingException, DuplicateReviewException {

        if (rating < 1 || rating > 10)
            throw new InvalidRatingException("Rating must be between 1 and 10, got: " + rating);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("No user found with id: " + userId));
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new NotFoundException("No movie found with id: " + movieId));

        for (Review review : reviewRepository.findByUser_Id(userId)) {
            if (review.getMovie().getId().equals(movieId))
                throw new DuplicateReviewException("User " + userId + " has already reviewed movie " + movieId);
        }

        return reviewRepository.save(new Review(user, movie, rating));
    } // end of createReview()

    public boolean deleteReview(int userId, int id) throws NotFoundException, UnauthorizedActionException {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No review found with id: " + id));

        if (!review.getUser().getId().equals(userId))
            throw new UnauthorizedActionException("User " + userId + " is not authorized to delete review " + id);

        reviewRepository.delete(review);
        return true;
    } // end of deleteReview()

    public List<Review> getReviewsByMovie(int movieId) throws NotFoundException {
        if (movieRepository.findById(movieId).isEmpty())
            throw new NotFoundException("No movie found with id: " + movieId);

        return reviewRepository.findByMovie_Id(movieId);
    } // end of getReviewsByMovie()

    public List<Review> getReviewsByUser(int userId) throws NotFoundException {
        if (userRepository.findById(userId).isEmpty())
            throw new NotFoundException("No user found with id: " + userId);

        return reviewRepository.findByUser_Id(userId);
    } // end of getReviewsByUser()

    public Review updateReview(int userId, int id, int rating)
            throws NotFoundException, UnauthorizedActionException, InvalidRatingException {

        if (rating < 1 || rating > 10)
            throw new InvalidRatingException("Rating must be between 1 and 10, got: " + rating);

        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No review found with id: " + id));

        if (!review.getUser().getId().equals(userId))
            throw new UnauthorizedActionException("User " + userId + " is not authorized to update review " + id);

        review.setRating(rating);
        return reviewRepository.save(review);
    } // end of updateReview()

} // end of class