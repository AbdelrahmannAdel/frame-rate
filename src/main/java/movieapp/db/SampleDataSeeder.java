package movieapp.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SampleDataSeeder {

    private static final String INSERT_USER = """
            INSERT INTO users (username, email, password_hash)
            VALUES (?, ?, ?)
            ON CONFLICT (username) DO NOTHING
    """;

    private static final String INSERT_MOVIE = """
            INSERT INTO movies (tmdb_id, title, release_year, overview, runtime_minutes)
            VALUES (?, ?, ?, ?, ?)
            ON CONFLICT (tmdb_id) DO NOTHING
     """;

    public static void seed(Connection connection) throws SQLException {

        try (PreparedStatement ps = connection.prepareStatement(INSERT_USER)) {
            ps.setString(1, "ahmed2");
            ps.setString(2, "ahmed2@example.com");
            ps.setString(3, "not2_a_real_hash_yet");
            ps.executeUpdate();
        }

        try (PreparedStatement ps = connection.prepareStatement(INSERT_MOVIE)) {
            ps.setInt(1, 13131);
            ps.setString(2, "MovieY");
            ps.setInt(3, 2001);
            ps.setString(4, "description Y");
            ps.setInt(5,120);
            ps.executeUpdate();
        }

        // reading users back
        String selectUsers = "SELECT id, username, email FROM users";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(selectUsers)) {

            System.out.println("Users in DB:");
            while (rs.next()) {
                System.out.printf("  id=%d, username=%s, email=%s%n",
                        rs.getInt("id"), rs.getString("username"), rs.getString("email"));
            }
        }

        // reading users back
        String selectMovies = "SELECT id, title, release_year FROM movies";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(selectMovies)) {

            System.out.println("Movies in DB:");
            while (rs.next()) {
                System.out.printf("  id=%d, title=%s, year=%d%n",
                        rs.getInt("id"), rs.getString("title"), rs.getInt("release_year"));
            }
        }
    } // end of seed()

} // end of class