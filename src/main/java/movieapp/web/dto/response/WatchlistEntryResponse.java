package movieapp.web.dto.response;

import movieapp.model.WatchlistEntry;

import java.time.LocalDateTime;

public class WatchlistEntryResponse {

    private final Integer id;
    private final Integer userId;
    private final Integer movieId;
    private final LocalDateTime addedAt;

    public WatchlistEntryResponse(WatchlistEntry entry) {
        this.id = entry.getId();
        this.userId = entry.getUser().getId();
        this.movieId = entry.getMovie().getId();
        this.addedAt = entry.getAddedAt();
    }

    public Integer getId() { return id; }
    public Integer getUserId() { return userId; }
    public Integer getMovieId() { return movieId; }
    public LocalDateTime getAddedAt() { return addedAt; }

} // end of class