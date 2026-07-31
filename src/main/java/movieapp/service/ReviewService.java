package movieapp.service;

import movieapp.db.MovieRepository;
import movieapp.db.ReviewRepository;
import movieapp.db.UserRepository;
import movieapp.exception.DuplicateReviewException;
import movieapp.exception.InvalidRatingException;
import movieapp.exception.NotFoundException;
import movieapp.exception.UnauthorizedActionException;
import movieapp.model.Review;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class ReviewService {

    private final Connection connection;

    public ReviewService(Connection connection){
        this.connection = connection;
    }

    public Review createReview(int userId, int movieId, int rating) throws SQLException, InvalidRatingException, DuplicateReviewException {
        ReviewRepository reviewRepository = new ReviewRepository();

        // rating should be between 1 - 10
        if (rating < 1 || rating > 10) {
            throw new InvalidRatingException("Rating must be between 1 and 10, got: " + rating);
        }

        // loop through the list of reviews by user and check if it's duplicate
        List<Review> reviewList = reviewRepository.findByUser(connection,userId);
        for (Review review: reviewList){
            if (review.getMovieId() == movieId)
                throw new DuplicateReviewException("User " + userId + " has already reviewed movie " + movieId);
        }

        return reviewRepository.create(connection, userId, movieId, rating);
    } // end of createReview()

    public boolean deleteReview(int userId, int id) throws NotFoundException, UnauthorizedActionException, SQLException {
        ReviewRepository reviewRepository = new ReviewRepository();
        Review review = reviewRepository.findById(connection, id);

        // if review not found
        if (review == null)
            throw new NotFoundException("No review found with id: " + id);

        // if user id doesn't match
        if (review.getUserId() != userId)
            throw new UnauthorizedActionException("User " + userId + " is not authorized to delete review " + id);

        return reviewRepository.delete(connection, id);
    } // end of deleteReview()

    public List<Review> getReviewsByMovie(int movieId) throws SQLException, NotFoundException {
        MovieRepository movieRepository = new MovieRepository();
        if (movieRepository.findById(connection, movieId) == null)
            throw new NotFoundException("No movie found with id: " + movieId);

        ReviewRepository reviewRepository = new ReviewRepository();
        return reviewRepository.findByMovie(connection, movieId);
    } // end of getReviewsByMovie()

    public List<Review> getReviewsByUser(int userId) throws SQLException, NotFoundException {
        UserRepository userRepository = new UserRepository();
        if (userRepository.findById(connection, userId) == null)
            throw new NotFoundException("No user found with id: " + userId);

        ReviewRepository reviewRepository = new ReviewRepository();
        return reviewRepository.findByUser(connection,userId);
    } // end of getReviewsByUser()

} // end of class
