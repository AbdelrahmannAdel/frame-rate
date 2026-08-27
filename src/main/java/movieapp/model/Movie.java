package movieapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "movies")
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "tmdb_id", nullable = false, unique = true)
    private int tmdbId;

    @Column(nullable = false)
    private String title;

    @Column(name = "release_year")
    private Integer releaseYear;

    @Column(name = "poster_path")
    private String posterPath;

    @Column
    private String overview;

    @Column(name = "runtime_minutes")
    private Integer runtimeMinutes;

    @Column(name = "cached_at")
    private LocalDateTime cachedAt;

    public Movie(){
    }

    public Movie(int tmdbId, String title, Integer releaseYear, String posterPath, String overview, Integer runtimeMinutes) {
        this.tmdbId = tmdbId;
        this.title = title;
        this.releaseYear = releaseYear;
        this.posterPath = posterPath;
        this.overview = overview;
        this.runtimeMinutes = runtimeMinutes;
    }

    @PrePersist
    protected void onCreate() {
        this.cachedAt = LocalDateTime.now();
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setTmdbId(int tmdbId) {
        this.tmdbId = tmdbId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setReleaseYear(Integer releaseYear) {
        this.releaseYear = releaseYear;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public void setRuntimeMinutes(Integer runtimeMinutes) {
        this.runtimeMinutes = runtimeMinutes;
    }

    public void setCachedAt(LocalDateTime cachedAt) {
        this.cachedAt = cachedAt;
    }

    public Integer getId() {
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
