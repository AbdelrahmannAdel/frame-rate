package movieapp;

import movieapp.db.DatabaseConfig;
import movieapp.db.SchemaInitializer;
import movieapp.exception.*;
import movieapp.model.Follow;
import movieapp.model.Movie;
import movieapp.model.Review;
import movieapp.model.User;
import movieapp.model.WatchlistEntry;
import movieapp.service.FollowService;
import movieapp.service.MovieService;
import movieapp.service.ReviewService;
import movieapp.service.UserService;
import movieapp.service.WatchlistService;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            SchemaInitializer.initialize(conn);

            UserService userService = new UserService(conn);
            MovieService movieService = new MovieService(conn);
            ReviewService reviewService = new ReviewService(conn);
            WatchlistService watchlistService = new WatchlistService(conn);
            FollowService followService = new FollowService(conn);

            // ===== HAPPY PATH =====
            System.out.println("=== HAPPY PATH ===");

            User userA = userService.registerUser("alice", "alice@example.com", "hashA");
            System.out.println("Registered: " + userA.getUsername() + " (id=" + userA.getId() + ")");

            User userB = userService.registerUser("bob", "bob@example.com", "hashB");
            System.out.println("Registered: " + userB.getUsername() + " (id=" + userB.getId() + ")");

            Movie movie = movieService.searchAndImport("Inception");
            System.out.println("Created movie: " + movie.getTitle() + " (id=" + movie.getId() + ", runtimeMinutes=" + movie.getRuntimeMinutes() + ")");

            Review review = reviewService.createReview(userA.getId(), movie.getId(), 9);
            System.out.println("Created review: rating=" + review.getRating() + " (id=" + review.getId() + ")");

            WatchlistEntry entry = watchlistService.addToWatchlist(userA.getId(), movie.getId());
            System.out.println("Added to watchlist (id=" + entry.getId() + ")");

            Follow follow = followService.followUser(userA.getId(), userB.getId());
            System.out.println("Follow created: " + follow.getFollowerId() + " -> " + follow.getFolloweeId());

            // ===== BUSINESS RULE VIOLATIONS =====
            System.out.println("\n=== BUSINESS RULE VIOLATIONS (each should throw) ===");

            try {
                reviewService.createReview(userA.getId(), movie.getId(), 15);
                System.out.println("FAIL: expected InvalidRatingException");
            } catch (InvalidRatingException e) {
                System.out.println("OK - InvalidRatingException: " + e.getMessage());
            }

            try {
                reviewService.createReview(userA.getId(), movie.getId(), 5);
                System.out.println("FAIL: expected DuplicateReviewException");
            } catch (DuplicateReviewException e) {
                System.out.println("OK - DuplicateReviewException: " + e.getMessage());
            }

            try {
                watchlistService.addToWatchlist(userA.getId(), movie.getId());
                System.out.println("FAIL: expected DuplicateWatchlistException");
            } catch (DuplicateWatchlistException e) {
                System.out.println("OK - DuplicateWatchlistException: " + e.getMessage());
            }

            try {
                followService.followUser(userA.getId(), userA.getId());
                System.out.println("FAIL: expected SelfFollowException");
            } catch (SelfFollowException e) {
                System.out.println("OK - SelfFollowException: " + e.getMessage());
            }

            try {
                followService.followUser(userA.getId(), userB.getId());
                System.out.println("FAIL: expected DuplicateFollowException");
            } catch (DuplicateFollowException e) {
                System.out.println("OK - DuplicateFollowException: " + e.getMessage());
            }

            try {
                userService.registerUser("alice", "different@example.com", "hashX");
                System.out.println("FAIL: expected DuplicateUsernameException");
            } catch (DuplicateUsernameException e) {
                System.out.println("OK - DuplicateUsernameException: " + e.getMessage());
            }

            try {
                userService.registerUser("someoneelse", "alice@example.com", "hashX");
                System.out.println("FAIL: expected DuplicateEmailException");
            } catch (DuplicateEmailException e) {
                System.out.println("OK - DuplicateEmailException: " + e.getMessage());
            }

            try {
                movieService.createMovie(27205, "Inception Duplicate", 2010, null, null, null);
                System.out.println("FAIL: expected DuplicateMovieException");
            } catch (DuplicateMovieException e) {
                System.out.println("OK - DuplicateMovieException: " + e.getMessage());
            }

            try {
                reviewService.deleteReview(userA.getId(), 999999);
                System.out.println("FAIL: expected NotFoundException");
            } catch (NotFoundException e) {
                System.out.println("OK - NotFoundException: " + e.getMessage());
            }

            try {
                reviewService.deleteReview(userB.getId(), review.getId());
                System.out.println("FAIL: expected UnauthorizedActionException");
            } catch (UnauthorizedActionException e) {
                System.out.println("OK - UnauthorizedActionException: " + e.getMessage());
            }

            try {
                followService.unfollowUser(userB.getId(), userA.getId());
                System.out.println("FAIL: expected NotFoundException");
            } catch (NotFoundException e) {
                System.out.println("OK - NotFoundException: " + e.getMessage());
            }

            // ===== CASCADE DELETE TESTS =====
            System.out.println("\n=== CASCADE DELETES ===");

            boolean movieDeleted = movieService.deleteMovie(movie.getId());
            System.out.println("Movie deleted (with its review + watchlist entry cascaded): " + movieDeleted);

            boolean userADeleted = userService.deleteUser(userA.getId());
            System.out.println("User A deleted (with their follow relationships cascaded): " + userADeleted);

            boolean userBDeleted = userService.deleteUser(userB.getId());
            System.out.println("User B deleted: " + userBDeleted);

            System.out.println("\n=== ALL TESTS COMPLETED ===");

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        } catch (IOException | InterruptedException e) {
            System.out.println("TMDB API error: " + e.getMessage());
        } catch (InvalidRatingException | DuplicateReviewException | DuplicateWatchlistException
                 | SelfFollowException | DuplicateFollowException | DuplicateUsernameException
                 | DuplicateEmailException | DuplicateMovieException | NotFoundException
                 | UnauthorizedActionException e) {
            System.out.println("Unexpected error in happy path: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }

    } // end of main()
} // end of class