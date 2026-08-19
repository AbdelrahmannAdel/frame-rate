package movieapp.model;

public class RatedMovie {

    private final Movie movie;
    private final double averageRating;

    public RatedMovie(Movie movie, double averageRating) {
        this.movie = movie;
        this.averageRating = averageRating;
    }

    public Movie getMovie() {
        return movie;
    }

    public double getAverageRating() {
        return averageRating;
    }

} // end of class