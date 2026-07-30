package movieapp.api;

import movieapp.exception.DuplicateMovieException;
import movieapp.model.Movie;
import movieapp.service.MovieService;

import java.sql.Connection;
import java.sql.SQLException;

// maps a movie result from tmdb to a movie object
// creates a new movie in the database
public class TmdbMovieMapper {
    public static Movie importMovie(Connection connection, TmdbMovieResult result) throws SQLException, DuplicateMovieException {

        // get the release year only from the release date
        Integer releaseYear = null;
        if (result.getReleaseDate() != null && result.getReleaseDate().length() >= 4)
            releaseYear = Integer.parseInt(result.getReleaseDate().substring(0, 4));

        // create a new movie object and create it in the db
        MovieService movieService = new MovieService(connection);
        return movieService.createMovie(
                result.getTmdbId(),
                result.getTitle(),
                releaseYear,
                result.getPosterPath(),
                result.getOverview(),
                null // runtime_minutes not available from search results
        );
    } // end of TmdbMovieMapper()
}