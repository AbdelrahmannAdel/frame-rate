package movieapp.service;

import movieapp.db.MovieRepository;
import movieapp.db.ReviewRepository;
import movieapp.db.WatchlistRepository;
import movieapp.exception.DuplicateMovieException;
import movieapp.exception.NotFoundException;
import movieapp.model.Movie;
import movieapp.model.Review;
import movieapp.model.WatchlistEntry;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class MovieService {

    private final Connection connection;

    public MovieService(Connection connection){
        this.connection = connection;
    }

    public Movie createMovie(int tmdbId, String title, Integer releaseYear, String posterPath, String overview, Integer runtimeMinutes) throws SQLException, DuplicateMovieException {
        MovieRepository movieRepository = new MovieRepository();

        if (movieRepository.findByTmdbId(connection,tmdbId) != null)
            throw new DuplicateMovieException("Movie already exists with tmdb_id: " + tmdbId);

        return movieRepository.create(connection, tmdbId, title, releaseYear, posterPath, overview, runtimeMinutes);
    } // end of createMovie()

    public boolean deleteMovie(int id) throws SQLException, NotFoundException {
        MovieRepository movieRepository = new MovieRepository();
        ReviewRepository reviewRepository = new ReviewRepository();
        WatchlistRepository watchlistRepository = new WatchlistRepository();

        // check if movie exists
        if (movieRepository.findById(connection,id) == null)
            throw new NotFoundException("No movie found with id: " + id);

        // delete all review with for movie
        List<Review> reviewList = reviewRepository.findByMovie(connection, id);
        for (Review review: reviewList)
            reviewRepository.delete(connection, review.getId());

        // delete all watchlist entries for movie
        List<WatchlistEntry> watchList = watchlistRepository.findByMovie(connection,id);
        for (WatchlistEntry watchlistEntry: watchList)
            watchlistRepository.remove(connection, watchlistEntry.getId());

        return movieRepository.delete(connection,id);
    } // end of deleteMovie()

} // end of class