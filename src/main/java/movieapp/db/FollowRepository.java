package movieapp.db;

import movieapp.model.Follow;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings("DuplicatedCode")
public class FollowRepository {

    private static final String INSERT_FOLLOW = """
        INSERT INTO follows (follower_id, followee_id)
        VALUES (?, ?)
    """;

    private static final String SELECT_FOLLOW = """
        SELECT follower_id, followee_id, created_at
        FROM follows
        WHERE follower_id = ? AND followee_id = ?
    """;

    private static final String SELECT_FOLLOWERS = """
        SELECT follower_id, followee_id, created_at
        FROM follows
        WHERE followee_id = ?
    """;

    private static final String SELECT_FOLLOWING = """
        SELECT follower_id, followee_id, created_at
        FROM follows
        WHERE follower_id = ?
    """;

    private static final String DELETE_FOLLOW = """
        DELETE FROM follows
        WHERE follower_id = ? AND followee_id = ?
    """;

    public Follow follow(Connection connection, int followerId, int followeeId) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(INSERT_FOLLOW)) {
            ps.setInt(1, followerId);
            ps.setInt(2, followeeId);
            ps.executeUpdate();
        }

        try (PreparedStatement ps2 = connection.prepareStatement(SELECT_FOLLOW)) {
            ps2.setInt(1, followerId);
            ps2.setInt(2, followeeId);

            try (ResultSet resultSet = ps2.executeQuery()) {
                if (resultSet.next())
                    return mapRow(resultSet);
            }
        }
        return null;
    } // end of follow()

    public List<Follow> findFollowers(Connection connection, int userId) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(SELECT_FOLLOWERS)) {
            ps.setInt(1, userId);

            try (ResultSet resultSet = ps.executeQuery()) {
                List<Follow> followers = new ArrayList<>();
                while (resultSet.next()) {
                    followers.add(mapRow(resultSet));
                }
                return followers;
            }
        }
    } // end of findFollowers()

    public List<Follow> findFollowing(Connection connection, int userId) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(SELECT_FOLLOWING)) {
            ps.setInt(1, userId);

            try (ResultSet resultSet = ps.executeQuery()) {
                List<Follow> following = new ArrayList<>();
                while (resultSet.next()) {
                    following.add(mapRow(resultSet));
                }
                return following;
            }
        }
    } // end of findFollowing()

    public boolean unfollow(Connection connection, int followerId, int followeeId) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(DELETE_FOLLOW)) {
            ps.setInt(1, followerId);
            ps.setInt(2, followeeId);
            int result = ps.executeUpdate();

            return result > 0;
        }
    } // end of unfollow()

    // maps the current row of a ResultSet into a Follow object
    // pulled out into its own method since other methods need the exact same row-to-object logic
    private Follow mapRow(ResultSet resultSet) throws SQLException {
        return new Follow(
                resultSet.getInt("follower_id"),
                resultSet.getInt("followee_id"),
                resultSet.getTimestamp("created_at").toLocalDateTime()
        );
    } // end of mapRow()

} // end of class