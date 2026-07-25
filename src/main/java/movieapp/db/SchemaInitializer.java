package movieapp.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class SchemaInitializer {

    private static final String CREATE_USERS_TABLE = """
        CREATE TABLE IF NOT EXISTS users (
            id SERIAL PRIMARY KEY,
            username VARCHAR(50) UNIQUE NOT NULL,
            email VARCHAR(255) UNIQUE NOT NULL,
            password_hash VARCHAR(255) NOT NULL,
            created_at TIMESTAMP NOT NULL DEFAULT NOW()
        )
    """;

    private static final String CREATE_MOVIES_TABLE = """
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

    private static final String CREATE_REVIEWS_TABLE = """
        CREATE TABLE IF NOT EXISTS reviews (
            id SERIAL PRIMARY KEY,
            user_id INTEGER NOT NULL REFERENCES users(id),
            movie_id INTEGER NOT NULL REFERENCES movies(id),
            rating INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 10),
            created_at TIMESTAMP NOT NULL DEFAULT NOW(),
            UNIQUE(user_id, movie_id)
        )
    """;

    private static final String CREATE_WATCHLIST_TABLE = """
        CREATE TABLE IF NOT EXISTS watchlist (
            id SERIAL PRIMARY KEY,
            user_id INTEGER NOT NULL REFERENCES users(id),
            movie_id INTEGER NOT NULL REFERENCES movies(id),
            added_at TIMESTAMP NOT NULL DEFAULT NOW(),
            UNIQUE(user_id, movie_id)
        )
    """;

    private static final String CREATE_FOLLOWS_TABLE = """
        CREATE TABLE IF NOT EXISTS follows (
            follower_id INTEGER NOT NULL REFERENCES users(id),
            followee_id INTEGER NOT NULL REFERENCES users(id),
            created_at TIMESTAMP NOT NULL DEFAULT NOW(),
            PRIMARY KEY (follower_id, followee_id)
        )
    """;

    public static void initialize(Connection connection) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(CREATE_USERS_TABLE);
            System.out.println("users table ready");

            stmt.execute(CREATE_MOVIES_TABLE);
            System.out.println("movies table ready");

            stmt.execute(CREATE_REVIEWS_TABLE);
            System.out.println("reviews table ready");

            stmt.execute(CREATE_WATCHLIST_TABLE);
            System.out.println("watchlist table ready");

            stmt.execute(CREATE_FOLLOWS_TABLE);
            System.out.println("follows table ready");
        }
    } // end of initialize()

} // end of class