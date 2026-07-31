package movieapp.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

// represents the fields we actually need from TMDB's movie details response
// (a single flat object, not a search-results envelope)

@JsonIgnoreProperties(ignoreUnknown = true)
public class TmdbMovieDetails {

    @JsonProperty("id")
    private int tmdbId;

    private Integer runtime;

    public TmdbMovieDetails() {
    }

    public int getTmdbId() {
        return tmdbId;
    }

    public void setTmdbId(int tmdbId) {
        this.tmdbId = tmdbId;
    }

    public Integer getRuntime() {
        return runtime;
    }

    public void setRuntime(Integer runtime) {
        this.runtime = runtime;
    }

} // end of class