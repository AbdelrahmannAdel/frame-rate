package movieapp.model;

import java.time.LocalDateTime;

public class Movie {

    private final int id;
    private final int tmdbId;
    private final String title;
    private final Integer releaseYear;
    private final String posterPath;
    private final String overview;
    private final Integer runtimeMinutes;
    private final LocalDateTime cachedAt;

    public Movie(int id, int tmdbId, String title, Integer releaseYear, String posterPath, String overview, Integer runtimeMinutes, LocalDateTime cachedAt) {
        this.id = id;
        this.tmdbId = tmdbId;
        this.title = title;
        this.releaseYear = releaseYear;
        this.posterPath = posterPath;
        this.overview = overview;
        this.runtimeMinutes = runtimeMinutes;
        this.cachedAt = cachedAt;
    }

    public int getId() {
        return id;
    }

    public int getTmdbId() {
        return tmdbId;
    }

    public String getTitle() {
        return title;
    }

    public Integer getReleaseYear() {
        return releaseYear;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public String getOverview() {
        return overview;
    }

    public Integer getRuntimeMinutes() {
        return runtimeMinutes;
    }

    public LocalDateTime getCachedAt() {
        return cachedAt;
    }

} // end of class