package movieapp.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

// represents the fields we actually need from TMDB's movie details response
// (a single flat object, not a search-results envelope)
//
// UPDATE: expanded beyond just tmdbId/runtime to also carry title, overview,
// posterPath, and releaseDate -- this lets the import route fetch a movie's
// full details in ONE call and import directly, without needing to have a
// TmdbMovieResult (from a prior search) sitting around

@JsonIgnoreProperties(ignoreUnknown = true)
public class TmdbMovieDetails {

    @JsonProperty("id")
    private int tmdbId;

    private String title;
    private String overview;

    @JsonProperty("poster_path")
    private String posterPath;

    @JsonProperty("release_date")
    private String releaseDate;

    private Integer runtime;

    public TmdbMovieDetails() {
    }

    public int getTmdbId() {
        return tmdbId;
    }

    public void setTmdbId(int tmdbId) {
        this.tmdbId = tmdbId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public Integer getRuntime() {
        return runtime;
    }

    public void setRuntime(Integer runtime) {
        this.runtime = runtime;
    }

} // end of class