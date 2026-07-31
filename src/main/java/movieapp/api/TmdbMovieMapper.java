package movieapp.api;

import movieapp.api.dto.TmdbMovieResult;
import movieapp.exception.DuplicateMovieException;
import movieapp.model.Movie;
import movieapp.service.MovieService;

import java.sql.Connection;
import java.sql.SQLException;

// maps a movie result from tmdb to a movie object
// creates a new movie in the database
public class TmdbMovieMapper {

    public static Movie importMovie(Connection connection, TmdbMovieResult movieResult, Integer runtimeMinutes) throws SQLException, DuplicateMovieException {

        // get the release year only from the release date
        Integer releaseYear = null;
        if (movieResult.getReleaseDate() != null && movieResult.getReleaseDate().length() >= 4)
            releaseYear = Integer.parseInt(movieResult.getReleaseDate().substring(0, 4));

        // create a new movie object and create it in the db
        MovieService movieService = new MovieService(connection);
        return movieService.createMovie(
                movieResult.getTmdbId(),
                movieResult.getTitle(),
                releaseYear,
                movieResult.getPosterPath(),
                movieResult.getOverview(),
                runtimeMinutes
        );

    } // end of importMovie()

} // end of class