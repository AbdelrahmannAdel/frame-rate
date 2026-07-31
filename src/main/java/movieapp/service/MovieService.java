package movieapp.service;

import movieapp.api.TmdbClient;
import movieapp.api.dto.TmdbMovieDetails;
import movieapp.api.TmdbMovieMapper;
import movieapp.api.dto.TmdbMovieResult;
import movieapp.db.MovieRepository;
import movieapp.db.ReviewRepository;
import movieapp.db.WatchlistRepository;
import movieapp.exception.DuplicateMovieException;
import movieapp.exception.NotFoundException;
import movieapp.model.Movie;
import movieapp.model.Review;
import movieapp.model.WatchlistEntry;

import java.io.IOException;
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

    public Movie searchAndImport(String title) throws IOException, InterruptedException, DuplicateMovieException, SQLException {
        TmdbClient tmdbClient = new TmdbClient();

        // search tmdb by title and parse the results into a list
        List<TmdbMovieResult> movieResults = tmdbClient.parseSearchResults(tmdbClient.searchMovies(title));

        // pick the first match from the search results
        TmdbMovieResult firstResult = movieResults.getFirst();

        // fetch full details for that specific movie (search results don't include runtime)
        TmdbMovieDetails movieDetails = tmdbClient.parseMovieDetails(tmdbClient.getMovieDetails(firstResult.getTmdbId()));

        // map the search result + real runtime into our db via MovieService
        return TmdbMovieMapper.importMovie(connection, firstResult, movieDetails.getRuntime());
    } // end of searchAndImport()

} // end of class