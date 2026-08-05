package movieapp.model;

import movieapp.model.Movie;

// pairs a Movie with its computed average rating -- NOT a database row,
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