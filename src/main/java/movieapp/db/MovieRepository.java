package movieapp.db;

import movieapp.model.Movie;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.ArrayList;

public class MovieRepository {

    private static final String INSERT_MOVIE = """
        INSERT INTO movies (tmdb_id, title, release_year, poster_path, overview, runtime_minutes)
        VALUES (?, ?, ?, ?, ?, ?)
    """;

    private static final String SELECT_MOVIE_BY_ID = """
        SELECT id, tmdb_id, title, release_year, poster_path, overview, runtime_minutes, cached_at
        FROM movies
        WHERE id = ?
    """;

    private static final String SELECT_MOVIE_BY_TMDB_ID = """
        SELECT id, tmdb_id, title, release_year, poster_path, overview, runtime_minutes, cached_at
        FROM movies
        WHERE tmdb_id = ?
    """;

    private static final String SELECT_ALL_MOVIES = """
        SELECT id, tmdb_id, title, release_year, poster_path, overview, runtime_minutes, cached_at
        FROM movies
    """;

    private static final String DELETE_MOVIE = """
        DELETE FROM movies
        WHERE id = ?
    """;

    // inserts a movie into the movies table
    public Movie create(
            Connection connection, int tmdbId, String title, Integer releaseYear, String posterPath, String overview, Integer runtimeMinutes)
            throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(INSERT_MOVIE, Statement.RETURN_GENERATED_KEYS)){

            // set variables
            ps.setInt(1, tmdbId);
            ps.setString(2, title);
            ps.setObject(3, releaseYear); // setObject used since releaseYear is Integer
            ps.setString(4, posterPath);
            ps.setString(5, overview);
            ps.setObject(6, runtimeMinutes); // setObject used since runtimeMinutes is Integer

            // execute the insert
            ps.executeUpdate();

            // resultSet contains the generated keys (the serial id)
            try(ResultSet resultSet = ps.getGeneratedKeys()){

                // resultSet is positioned before the first row so we move it to the next position (the id)
                if (resultSet.next()){
                    int id = resultSet.getInt("id");
                    return findById(connection, id);
                }
            }

        }
        return null;
    } // end of create()

    public Movie findById(Connection connection, int id) throws SQLException {

        // prepare the statement
        try (PreparedStatement ps = connection.prepareStatement(SELECT_MOVIE_BY_ID)){

            // set the variables
            ps.setInt(1, id);

            // execute the query, returns a result set
            try (ResultSet resultSet = ps.executeQuery()){
                if (resultSet.next()) {
                    return mapRow(resultSet);
                }
            }
        }
        return null;
    } // end of findById()

    public Movie findByTmdbId(Connection connection, int tmdbId) throws SQLException {

        // prepare the statement
        try (PreparedStatement ps = connection.prepareStatement(SELECT_MOVIE_BY_TMDB_ID)){

            // set the variables
            ps.setInt(1,tmdbId);

            // execute the query, returns a result set
            try (ResultSet resultSet = ps.executeQuery()){
                if (resultSet.next()) {
                    return mapRow(resultSet);
                }
            }
        }
        return null;
    } // end of findByTmdbId()

    public List<Movie> findAll(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()){
            try (ResultSet resultSet = statement.executeQuery(SELECT_ALL_MOVIES)){
                List<Movie> moviesList = new ArrayList<>();

                // loop since multiple rows can match, unlike findById/findByTmdbId
                while (resultSet.next()){
                    moviesList.add(mapRow(resultSet));
                }
                return moviesList;
            }
        }
    } // end of findAll()

    public boolean delete(Connection connection, int id) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(DELETE_MOVIE)) {
            ps.setInt(1, id);

            // executeUpdate() returns rows affected, 1 if deleted, 0 if no match
            int result = ps.executeUpdate();

            return result > 0;
        }
    } // end of delete()

    // maps the current row of a ResultSet into a Movie object
    // pulled out into its own method since findById, findByTmdbId, and findAll
    // all needed the exact same row-to-object logic
    private Movie mapRow(ResultSet resultSet) throws SQLException {
        return new Movie(
                resultSet.getInt("id"),
                resultSet.getInt("tmdb_id"),
                resultSet.getString("title"),
                resultSet.getObject("release_year", Integer.class),
                resultSet.getString("poster_path"),
                resultSet.getString("overview"),
                resultSet.getObject("runtime_minutes", Integer.class),
                resultSet.getTimestamp("cached_at").toLocalDateTime()
        );
    } // end of mapRow()

} // end of class