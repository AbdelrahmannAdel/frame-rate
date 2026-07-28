package movieapp.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

// represents a search response
// the response is wrapper object with an array of movies
@JsonIgnoreProperties(ignoreUnknown = true)
public class TmdbSearchResponse {

    private List<TmdbMovieResult> results;

    public List<TmdbMovieResult> getResults() {
        return results;
    }

    public void setResults(List<TmdbMovieResult> results) {
        this.results = results;
    }

} // end of class