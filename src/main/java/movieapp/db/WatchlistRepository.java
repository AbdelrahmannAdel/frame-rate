package movieapp.db;

import movieapp.model.WatchlistEntry;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.ArrayList;

public class WatchlistRepository {

    private static final String INSERT_WATCHLIST_ENTRY = """
        INSERT INTO watchlist (user_id, movie_id)
        VALUES (?, ?)
    """;

    private static final String SELECT_WATCHLIST_ENTRY_BY_ID = """
        SELECT id, user_id, movie_id, added_at
        FROM watchlist
        WHERE id = ?
    """;

    private static final String SELECT_WATCHLIST_BY_USER = """
        SELECT id, user_id, movie_id, added_at
        FROM watchlist
        WHERE user_id = ?
    """;

    private static final String DELETE_WATCHLIST_ENTRY = """
        DELETE FROM watchlist
        WHERE id = ?
    """;

    private static final String SELECT_WATCHLIST_BY_MOVIE = """
        SELECT id, user_id, movie_id, added_at
        FROM watchlist
        WHERE movie_id = ?
    """;

    public WatchlistEntry add(Connection connection, int userId, int movieId) throws SQLException {
        try(PreparedStatement ps = connection.prepareStatement(INSERT_WATCHLIST_ENTRY, Statement.RETURN_GENERATED_KEYS)){
            ps.setInt(1, userId);
            ps.setInt(2, movieId);
            ps.executeUpdate();

            try (ResultSet resultSet = ps.getGeneratedKeys()){
                if (resultSet.next()){
                    int id = resultSet.getInt("id");
                    return findById(connection, id);
                }
            }

        }
        return null;
    } // end of add()

    public List<WatchlistEntry> findByUser(Connection connection, int userId) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(SELECT_WATCHLIST_BY_USER)) {
            ps.setInt(1, userId);
            try (ResultSet resultSet = ps.executeQuery()){
                List<WatchlistEntry> watchList = new ArrayList<>();
                while (resultSet.next())
                    watchList.add(mapRow(resultSet));
                return watchList;
            }
        }
    } // end of findByUser()

    public boolean remove(Connection connection, int id) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(DELETE_WATCHLIST_ENTRY)) {
            ps.setInt(1, id);
            int result = ps.executeUpdate();

            return result > 0;
        }
    } // end of remove()

    // findById is private here since it's used only by internal methods
    public WatchlistEntry findById(Connection connection, int id) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(SELECT_WATCHLIST_ENTRY_BY_ID)) {
            ps.setInt(1, id);

            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next()) {
                    return mapRow(resultSet);
                }
            }
        }
        return null;
    } // end of findById()

    public List<WatchlistEntry> findByMovie(Connection connection, int movieId) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(SELECT_WATCHLIST_BY_MOVIE)) {
            ps.setInt(1, movieId);
            try (ResultSet resultSet = ps.executeQuery()){
                List<WatchlistEntry> watchList = new ArrayList<>();
                while (resultSet.next())
                    watchList.add(mapRow(resultSet));
                return watchList;
            }
        }
    } // end of findByMovie()

    // maps the current row of a ResultSet into a WatchlistEntry object
    // pulled out into its own method since other methods need the exact same row-to-object logic
    private WatchlistEntry mapRow(ResultSet resultSet) throws SQLException {
        return new WatchlistEntry(
                resultSet.getInt("id"),
                resultSet.getInt("user_id"),
                resultSet.getInt("movie_id"),
                resultSet.getTimestamp("added_at").toLocalDateTime()
        );
    } // end of mapRow

} // end of class
