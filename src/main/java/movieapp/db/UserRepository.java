package movieapp.db;

import movieapp.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class UserRepository {

    private static final String INSERT_USER = """
        INSERT INTO users (username, email, password_hash)
        VALUES (?, ?, ?)
    """;

    // inserts a user into the users table
    public User createUser(Connection connection, String username, String email, String passwordHash) throws SQLException {

        // RETURN_GENERATED_KEY returns back auto generated values (serial id in this case)
        try (PreparedStatement ps = connection.prepareStatement(INSERT_USER, Statement.RETURN_GENERATED_KEYS)) {

            // set variables
            ps.setString(1, username);
            ps.setString(2, email);
            ps.setString(3, passwordHash);

            // execute the insert
            ps.executeUpdate();

            // resultSet contains the generated keys (the serial id)
            try (ResultSet resultSet = ps.getGeneratedKeys()) {

                // resultSet is positioned before the first row so we move it to the next position (the id)
                // .next() returns true if it moved to the next object, false if there's no next object
                if (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    return findById(connection, id);
                }
            }
        }
        return null;
    } // end of createUser()

    private static final String SELECT_USER_BY_ID = """
        SELECT id, username, email, password_hash, created_at
        FROM users
        WHERE id = ?
    """;

    public User findById(Connection connection, int id) throws SQLException {

        // prepare the statement
        try (PreparedStatement ps = connection.prepareStatement(SELECT_USER_BY_ID)) {

            // set the variables
            ps.setInt(1, id);

            // execute the query, returns a result set
            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next()) {
                    return new User(
                            resultSet.getInt("id"),
                            resultSet.getString("username"),
                            resultSet.getString("email"),
                            resultSet.getString("password_hash"),
                            resultSet.getTimestamp("created_at").toLocalDateTime()
                    );
                }
            }
        }
        return null;
    }


} // end of class