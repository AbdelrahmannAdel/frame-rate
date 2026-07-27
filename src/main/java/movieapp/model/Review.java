package movieapp.model;

import java.time.LocalDateTime;

public class Review {
    // id, user_id, movie_id, rating, created_at

    private final int id;
    private final int userId;
    private final int movieId;
    private final int rating;
    private final LocalDateTime createdAt;


    public Review(int id, int userId, int movieId, int rating, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.movieId = movieId;
        this.rating = rating;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
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
