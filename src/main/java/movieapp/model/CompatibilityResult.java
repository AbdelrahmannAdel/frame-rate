package movieapp.model;

import java.util.List;

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