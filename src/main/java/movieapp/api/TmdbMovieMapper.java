package movieapp.api;

import movieapp.db.MovieRepository;
import movieapp.model.Movie;
import java.sql.Connection;
import java.sql.SQLException;

// maps a movie result from tmdb to a movie object
// creates a new movie in the database
public class TmdbMovieMapper {
    public static Movie importMovie(Connection connection, MovieRepository movieRepository, TmdbMovieResult result) throws SQLException {

        // get the release year only from the release date
        Integer releaseYear = null;
        if (result.getReleaseDate() != null && result.getReleaseDate().length() >= 4)
            releaseYear = Integer.parseInt(result.getReleaseDate().substring(0, 4));

        // create a new movie object and create it in the db
        return movieRepository.create(
                connection,
                result.getTmdbId(),
                result.getTitle(),
                releaseYear,
                result.getPosterPath(),
                result.getOverview(),
                null // runtime_minutes not available from search results
        );
    } // end of TmdbMovieMapper()
}