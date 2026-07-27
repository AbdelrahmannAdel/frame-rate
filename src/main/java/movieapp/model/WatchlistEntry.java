package movieapp.model;

import java.time.LocalDateTime;

public class WatchlistEntry {

    private final int id;
    private final int userId;
    private final int movieId;
    private final LocalDateTime addedAt;

    public WatchlistEntry(int id, int userId, int movieId, LocalDateTime addedAt) {
        this.id = id;
        this.userId = userId;
        this.movieId = movieId;
        this.addedAt = addedAt;
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

    public LocalDateTime getAddedAt() {
        return addedAt;
    }

} // end of class