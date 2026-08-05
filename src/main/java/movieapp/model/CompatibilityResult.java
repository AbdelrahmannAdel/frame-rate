package movieapp.model;

import java.util.List;

// the full result of a compatibility check between two users --
// Double (not double) for compatibilityScore since it can be null
// when there isn't enough shared data yet to compute a meaningful score
public class CompatibilityResult {

    private final Double compatibilityScore;
    private final List<SharedMovieRating> sharedMovies;

    public CompatibilityResult(Double compatibilityScore, List<SharedMovieRating> sharedMovies) {
        this.compatibilityScore = compatibilityScore;
        this.sharedMovies = sharedMovies;
    }

    public Double getCompatibilityScore() {
        return compatibilityScore;
    }

    public List<SharedMovieRating> getSharedMovies() {
        return sharedMovies;
    }

} // end of class