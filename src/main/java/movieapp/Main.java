package movieapp;

import movieapp.db.DatabaseConfig;
import movieapp.db.SchemaInitializer;
import movieapp.db.MovieRepository;
import movieapp.api.TmdbClient;
import movieapp.api.TmdbMovieResult;
import movieapp.api.TmdbMovieMapper;
import movieapp.exception.DuplicateMovieException;
import movieapp.model.Movie;
import java.sql.Connection;
import java.sql.SQLException;
import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            SchemaInitializer.initialize(conn);

            TmdbClient tmdbClient = new TmdbClient();
            MovieRepository movieRepository = new MovieRepository();

            String json = tmdbClient.searchMovies("Inception");
            List<TmdbMovieResult> results = tmdbClient.parseSearchResults(json);

            TmdbMovieResult firstResult = results.get(0);
            System.out.println("Importing: " + firstResult.getTitle() + " (tmdbId=" + firstResult.getTmdbId() + ")");

            Movie savedMovie = TmdbMovieMapper.importMovie(conn, firstResult);

            System.out.println("Saved to database: " + savedMovie.getTitle()
                    + " (id=" + savedMovie.getId()
                    + ", tmdbId=" + savedMovie.getTmdbId()
                    + ", releaseYear=" + savedMovie.getReleaseYear() + ")");

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        } catch (IOException | InterruptedException e) {
            System.out.println("TMDB API error: " + e.getMessage());
        } catch (DuplicateMovieException e) {
            throw new RuntimeException(e);
        }
    } // end of main
} // end of class