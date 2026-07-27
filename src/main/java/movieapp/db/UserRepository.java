package movieapp.db;

import movieapp.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.ArrayList;

public class UserRepository {

    private static final String INSERT_USER = """
        INSERT INTO users (username, email, password_hash)
        VALUES (?, ?, ?)
    """;

    private static final String SELECT_USER_BY_ID = """
        SELECT id, username, email, password_hash, created_at
        FROM users
        WHERE id = ?
    """;

    private static final String SELECT_USER_BY_USERNAME = """
        SELECT id, username, email, password_hash, created_at
        FROM users
        WHERE username = ?
    """;

    private static final String SELECT_ALL_USERS = """
        SELECT id, username, email, password_hash, created_at
        FROM users
    """;

    private static final String DELETE_USER = """
        DELETE FROM users
        WHERE id = ?
    """;

    // inserts a user into the users table
    public User create(Connection connection, String username, String email, String passwordHash) throws SQLException {

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
    } // end of create()

    // finds user by id
    public User findById(Connection connection, int id) throws SQLException {

        // prepare the statement
        try (PreparedStatement ps = connection.prepareStatement(SELECT_USER_BY_ID)) {

            // set the variables
            ps.setInt(1, id);

            // execute the query, returns a result set
            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next()) {

                    // return new user with the data from the result set
                    return mapRow(resultSet);
                }
            }
        }
        return null;
    }

    // finds user by username
    public User findByUsername(Connection connection, String username) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(SELECT_USER_BY_USERNAME)){
            ps.setString(1, username);

            try (ResultSet resultSet = ps.executeQuery()){
                if (resultSet.next()){
                    return mapRow(resultSet);
                }
            }
        }
        return null;
    } // end of findByUsername()

    // returns a list of all users
    public List<User> findAll(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()){
            try (ResultSet resultSet = statement.executeQuery(SELECT_ALL_USERS)){
                List<User> usersList = new ArrayList<>();
                while (resultSet.next()){
                    usersList.add(mapRow(resultSet));
                }
                return usersList;
            }
        }
    } // end of findAll()

    // deletes a user by id
    public boolean delete(Connection connection, int id) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(DELETE_USER)){
            ps.setInt(1, id);
            int result = ps.executeUpdate();    // executeUpdate() returns no of rows affected

            return result > 0;
        }
    } // end of delete()

    // maps the current row of a ResultSet into a User object
    // pulled out into its own method since findById, findByUsername, and findAll
    // all needed the exact same row-to-object logic
    private User mapRow(ResultSet resultSet) throws SQLException {
        return new User(
                resultSet.getInt("id"),
                resultSet.getString("username"),
                resultSet.getString("email"),
                resultSet.getString("password_hash"),
                resultSet.getTimestamp("created_at").toLocalDateTime()
        );
    } // end of mapRow()

} // end of class