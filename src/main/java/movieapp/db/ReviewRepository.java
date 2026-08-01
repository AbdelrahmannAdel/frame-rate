package movieapp.db;

import movieapp.model.Review;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.ArrayList;

public class ReviewRepository {

    private static final String INSERT_REVIEW = """
        INSERT INTO reviews (user_id, movie_id, rating)
        VALUES (?, ?, ?)
    """;

    private static final String SELECT_REVIEW_BY_ID = """
        SELECT id, user_id, movie_id, rating, created_at
        FROM reviews
        WHERE id = ?
    """;

    private static final String SELECT_REVIEWS_BY_USER = """
        SELECT id, user_id, movie_id, rating, created_at
        FROM reviews
        WHERE user_id = ?
    """;

    private static final String SELECT_REVIEWS_BY_MOVIE = """
        SELECT id, user_id, movie_id, rating, created_at
        FROM reviews
        WHERE movie_id = ?
    """;

    private static final String DELETE_REVIEW = """
        DELETE FROM reviews
        WHERE id = ?
    """;

    private static final String UPDATE_REVIEW = """
        UPDATE reviews
        SET rating = ?
        WHERE id = ?
    """;

    public Review create(Connection connection, int userId, int movieId, int rating) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(INSERT_REVIEW, Statement.RETURN_GENERATED_KEYS)){
            ps.setInt(1, userId);
            ps.setInt(2, movieId);
            ps.setInt(3, rating);

            ps.executeUpdate();
            try (ResultSet resultSet = ps.getGeneratedKeys()){
                if (resultSet.next()){
                    int id = resultSet.getInt("id");
                    return findById(connection, id);
                }
            }
        }
        return null;
    } // end of create()

    public Review findById(Connection connection, int id) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(SELECT_REVIEW_BY_ID)) {
            ps.setInt(1, id);

            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next()) {
                    return mapRow(resultSet);
                }
            }
        }
        return null;
    } // end of findById()

    @SuppressWarnings("DuplicatedCode")
    public List<Review> findByUser(Connection connection, int userId) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(SELECT_REVIEWS_BY_USER)){
            ps.setInt(1, userId);
            try (ResultSet resultSet = ps.executeQuery()){
                List<Review> reviewsList = new ArrayList<>();
                while (resultSet.next())
                    reviewsList.add(mapRow(resultSet));
                return reviewsList;
            }
        }
    } // end of findByUser()

    @SuppressWarnings("DuplicatedCode")
    public List<Review> findByMovie(Connection connection, int movieId) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(SELECT_REVIEWS_BY_MOVIE)){
            ps.setInt(1, movieId);
            try (ResultSet resultSet = ps.executeQuery()){
                List<Review> reviewsList = new ArrayList<>();
                while (resultSet.next())
                    reviewsList.add(mapRow(resultSet));
                return reviewsList;
            }
        }
    } // end of findByMovie()

    public boolean delete(Connection connection, int id) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(DELETE_REVIEW)) {
            ps.setInt(1, id);
            int result = ps.executeUpdate();

            return result > 0;
        }
    } // end of delete()

    public Review update(Connection connection, int id, int rating) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(UPDATE_REVIEW)) {
            ps.setInt(1, rating);
            ps.setInt(2, id);

            ps.executeUpdate();
            return findById(connection, id);
        }
    } // end of update()

    // maps the current row of a ResultSet into a Review object
    // pulled out into its own method since other methods need the exact same row-to-object logic
    private Review mapRow(ResultSet resultSet) throws SQLException {
        return new Review(
                resultSet.getInt("id"),
                resultSet.getInt("user_id"),
                resultSet.getInt("movie_id"),
                resultSet.getInt("rating"),
                resultSet.getTimestamp("created_at").toLocalDateTime()
        );
    } // end of mapRow

} // end of class
