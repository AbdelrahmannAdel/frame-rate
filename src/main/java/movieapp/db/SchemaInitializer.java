package movieapp.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class SchemaInitializer {

    public static void initialize(Connection conn) throws SQLException {
        String createUsersTable = """
            CREATE TABLE IF NOT EXISTS users (
                id SERIAL PRIMARY KEY,
                username VARCHAR(50) UNIQUE NOT NULL,
                email VARCHAR(255) UNIQUE NOT NULL,
                password_hash VARCHAR(255) NOT NULL,
                created_at TIMESTAMP NOT NULL DEFAULT NOW()
            )
        """;

        String createMoviesTable = """
            CREATE TABLE IF NOT EXISTS movies (
                id SERIAL PRIMARY KEY,
                tmdb_id INTEGER UNIQUE NOT NULL,
                title VARCHAR(255) NOT NULL,
                release_year INTEGER,
                poster_path VARCHAR(255),
                overview TEXT,
                runtime_minutes INTEGER,
                cached_at TIMESTAMP NOT NULL DEFAULT NOW()
            )
        """;

        String createReviewsTable = """
            CREATE TABLE IF NOT EXISTS reviews (
                id SERIAL PRIMARY KEY,
                user_id INTEGER NOT NULL REFERENCES users(id),
                movie_id INTEGER NOT NULL REFERENCES movies(id),
                rating INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 10),
                created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                UNIQUE(user_id, movie_id)
            )
        """;

        String createWatchlistTable = """
            CREATE TABLE IF NOT EXISTS watchlist (
                id SERIAL PRIMARY KEY,
                user_id INTEGER NOT NULL REFERENCES users(id),
                movie_id INTEGER NOT NULL REFERENCES movies(id),
                added_at TIMESTAMP NOT NULL DEFAULT NOW(),
                UNIQUE(user_id, movie_id)
            )
        """;

        String createFollowsTable = """
            CREATE TABLE IF NOT EXISTS follows (
                follower_id INTEGER NOT NULL REFERENCES users(id),
                followee_id INTEGER NOT NULL REFERENCES users(id),
                created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                PRIMARY KEY (follower_id, followee_id)
            )
        """;

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createUsersTable);
            System.out.println("users table created");

            stmt.execute(createMoviesTable);
            System.out.println("movies table created");

            stmt.execute(createReviewsTable);
            System.out.println("reviews table created");

            stmt.execute(createWatchlistTable);
            System.out.println("watchlist table created");

            stmt.execute(createFollowsTable);
            System.out.println("follows table created");
        }
    }
}