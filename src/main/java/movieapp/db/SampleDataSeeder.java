package movieapp.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SampleDataSeeder {

    public static void seed(Connection conn) throws SQLException {
        // Insert a user
        String insertUser = """
            INSERT INTO users (username, email, password_hash)
            VALUES (?, ?, ?)
            ON CONFLICT (username) DO NOTHING
        """;
        try (PreparedStatement ps = conn.prepareStatement(insertUser)) {
            ps.setString(1, "ahmed");
            ps.setString(2, "ahmed@example.com");
            ps.setString(3, "not_a_real_hash_yet");
            ps.executeUpdate();
        }

        // Insert a movie
        String insertMovie = """
            INSERT INTO movies (tmdb_id, title, release_year, overview, runtime_minutes)
            VALUES (?, ?, ?, ?, ?)
            ON CONFLICT (tmdb_id) DO NOTHING
        """;
        try (PreparedStatement ps = conn.prepareStatement(insertMovie)) {
            ps.setInt(1, 27215);
            ps.setString(2, "MovieX");
            ps.setInt(3, 2010);
            ps.setString(4, "movie description");
            ps.setInt(5, 148);
            ps.executeUpdate();
        }

        System.out.println("Sample data inserted.");

        // Read it back
        String selectUsers = "SELECT id, username, email FROM users";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(selectUsers)) {
            System.out.println("Users in DB:");
            while (rs.next()) {
                System.out.printf("  id=%d, username=%s, email=%s%n",
                        rs.getInt("id"), rs.getString("username"), rs.getString("email"));
            }
        }

        String selectMovies = "SELECT id, title, release_year FROM movies";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(selectMovies)) {
            System.out.println("Movies in DB:");
            while (rs.next()) {
                System.out.printf("  id=%d, title=%s, year=%d%n",
                        rs.getInt("id"), rs.getString("title"), rs.getInt("release_year"));
            }
        }
    }
}