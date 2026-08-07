package movieapp.web.dto.response;

import movieapp.model.Review;

import java.time.LocalDateTime;

public class ReviewResponse {

    private final int id;
    private final int userId;
    private final String username;
    private final int movieId;
    private final int rating;
    private final LocalDateTime createdAt;

    public ReviewResponse(Review review, String username) {
        this.id = review.getId();
        this.userId = review.getUserId();
        this.username = username;
        this.movieId = review.getMovieId();
        this.rating = review.getRating();
        this.createdAt = review.getCreatedAt();
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public int getMovieId() {
        return movieId;
    }

    public int getRating() {
        return rating;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

} // end of class