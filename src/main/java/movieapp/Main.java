package movieapp;

import movieapp.db.DatabaseConfig;
import movieapp.db.SchemaInitializer;
import movieapp.db.UserRepository;
import movieapp.db.MovieRepository;
import movieapp.db.ReviewRepository;
import movieapp.db.WatchlistRepository;
import movieapp.db.FollowRepository;
import movieapp.model.User;
import movieapp.model.Movie;
import movieapp.model.Review;
import movieapp.model.WatchlistEntry;
import movieapp.model.Follow;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            SchemaInitializer.initialize(conn);

            UserRepository userRepository = new UserRepository();
            MovieRepository movieRepository = new MovieRepository();
            ReviewRepository reviewRepository = new ReviewRepository();
            WatchlistRepository watchlistRepository = new WatchlistRepository();
            FollowRepository followRepository = new FollowRepository();

            // create two users
            User alice = userRepository.create(conn, "alice", "alice@example.com", "fake_hash");
            User bob = userRepository.create(conn, "bob", "bob@example.com", "fake_hash");
            System.out.println("Created users: " + alice.getUsername() + " (id=" + alice.getId() + "), "
                    + bob.getUsername() + " (id=" + bob.getId() + ")");

            // create a movie
            Movie inception = movieRepository.create(conn, 27205, "Inception", 2010,
                    null, "A thief who steals secrets through dream-sharing.", 148);
            System.out.println("Created movie: " + inception.getTitle() + " (id=" + inception.getId() + ")");

            // alice reviews the movie
            Review review = reviewRepository.create(conn, alice.getId(), inception.getId(), 9);
            System.out.println("Created review: rating=" + review.getRating() + " by user " + review.getUserId());

            // bob adds it to his watchlist
            WatchlistEntry watchlistEntry = watchlistRepository.add(conn, bob.getId(), inception.getId());
            System.out.println("Added to watchlist: user " + watchlistEntry.getUserId()
                    + " -> movie " + watchlistEntry.getMovieId());

            // alice follows bob
            Follow follow = followRepository.follow(conn, alice.getId(), bob.getId());
            System.out.println("Follow created: " + follow.getFollowerId() + " -> " + follow.getFolloweeId());

            // read everything back
            List<Review> aliceReviews = reviewRepository.findByUser(conn, alice.getId());
            System.out.println("Alice's reviews count: " + aliceReviews.size());

            List<WatchlistEntry> bobWatchlist = watchlistRepository.findByUser(conn, bob.getId());
            System.out.println("Bob's watchlist count: " + bobWatchlist.size());

            List<Follow> bobFollowers = followRepository.findFollowers(conn, bob.getId());
            System.out.println("Bob's followers count: " + bobFollowers.size());

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    } // end of main
} // end of class