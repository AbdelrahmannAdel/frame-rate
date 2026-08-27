package movieapp.web.dto.response;

import movieapp.model.Review;

import java.time.LocalDateTime;

public class ReviewResponse {

    private final Integer id;
    private final Integer userId;
    private final String username;
    private final Integer movieId;
    private final int rating;
    private final LocalDateTime createdAt;

    public ReviewResponse(Review review, String username) {
        this.id = review.getId();
        this.userId = review.getUser().getId();
        this.username = username;
        this.movieId = review.getMovie().getId();
        this.rating = review.getRating();
        this.createdAt = review.getCreatedAt();
    }

    public Integer getId() {
        return id;
    }

    public Integer getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public Integer getMovieId() {
        return movieId;
    }

    public int getRating() {
        return rating;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

} // end of class