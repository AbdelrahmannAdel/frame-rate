package movieapp;

import io.javalin.Javalin;
import io.javalin.json.JavalinJackson;
import movieapp.db.DatabaseConfig;
import movieapp.db.WatchlistRepository;
import movieapp.exception.NotFoundException;
import movieapp.model.Movie;
import movieapp.model.Review;
import movieapp.model.User;
import movieapp.model.WatchlistEntry;
import movieapp.service.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        Javalin app = Javalin.create(config -> {

            config.jsonMapper(new JavalinJackson());

            config.routes.get("/", ctx ->
                    ctx.result("Hello from Javalin!")
            );

            config.routes.get("/movies/{id}", ctx -> {

                int id = Integer.parseInt(ctx.pathParam("id"));

                try (Connection conn = DatabaseConfig.getConnection()) {
                    MovieService movieService = new MovieService(conn);
                    Movie movie = movieService.getMovieById(id);
                    ctx.json(movie);
                } catch (NotFoundException e) {
                    ctx.status(404).result(e.getMessage());
                } catch (SQLException e) {
                    ctx.status(500).result("Database error: " + e.getMessage());
                }
            }); // end of GET /movies/{id}

            config.routes.get("/movies", ctx -> {
               try (Connection conn = DatabaseConfig.getConnection()){
                   MovieService movieService = new MovieService(conn);
                   List<Movie> moviesLis = movieService.getAllMovies();
                   ctx.json(moviesLis);
               } catch (SQLException e) {
                   ctx.status(500).result("Database error: " + e.getMessage());
               }
            }); // end of GET /movies

            config.routes.get("/movies/{id}/reviews", ctx -> {

                int id = Integer.parseInt(ctx.pathParam("id"));

                try (Connection conn = DatabaseConfig.getConnection()){
                    ReviewService reviewService = new ReviewService(conn);
                    List<Review> reviewsList = reviewService.getReviewsByMovie(id);
                    ctx.json(reviewsList);
                } catch (NotFoundException e) {
                    ctx.status(404).result(e.getMessage());
                } catch (SQLException e) {
                    ctx.status(500).result("Database error: " + e.getMessage());
                }
            }); // end of GET /movies/{id}/reviews

            config.routes.get("/users/{id}", ctx -> {
                int id = Integer.parseInt(ctx.pathParam("id"));

                try (Connection conn = DatabaseConfig.getConnection()) {

                    UserService userService = new UserService(conn);
                    ctx.json(userService.getUserById(id));

                } catch (NotFoundException e) {
                    ctx.status(404).result(e.getMessage());
                } catch (SQLException e) {
                    ctx.status(500).result("Database error: " + e.getMessage());
                }
            }); // end of get /users/{id}

            config.routes.get("/users/{id}/reviews", ctx -> {
                int id = Integer.parseInt(ctx.pathParam("id"));

                try (Connection conn = DatabaseConfig.getConnection()){

                    ReviewService reviewService = new ReviewService(conn);
                    List<Review> reviewsList = reviewService.getReviewsByUser(id);
                    ctx.json(reviewsList);

                } catch (NotFoundException e) {
                    ctx.status(404).result(e.getMessage());
                } catch (SQLException e) {
                    ctx.status(500).result("Database error: " + e.getMessage());
                }
            }); // end of GET /users/{id}/reviews

            config.routes.get("/users/{id}/watchlist", ctx -> {

                int id = Integer.parseInt(ctx.pathParam("id"));

                try (Connection conn = DatabaseConfig.getConnection()){
                    WatchlistService watchlistService = new WatchlistService(conn);
                    List<WatchlistEntry> watchList = watchlistService.getWatchlistByUser(id);
                    ctx.json(watchList);
                } catch (NotFoundException e) {
                    ctx.status(404).result(e.getMessage());
                } catch (SQLException e) {
                    ctx.status(500).result("Database error: " + e.getMessage());
                }
            }); // end of GET /users/{id}/watchlist

            config.routes.get("/users/{id}/following", ctx -> {
                int id = Integer.parseInt(ctx.pathParam("id"));

                try (Connection conn = DatabaseConfig.getConnection()){
                    FollowService followService = new FollowService(conn);
                    List<User> followingList = followService.getFollowing(id);
                    ctx.json(followingList);
                } catch (NotFoundException e) {
                    ctx.status(404).result(e.getMessage());
                } catch (SQLException e) {
                    ctx.status(500).result("Database error: " + e.getMessage());
                }
            }); // end of GET /users/{id}/following

            config.routes.get("/users/{id}/followers", ctx -> {
                int id = Integer.parseInt(ctx.pathParam("id"));

                try (Connection conn = DatabaseConfig.getConnection()){
                    FollowService followService = new FollowService(conn);
                    List<User> followersList = followService.getFollowers(id);
                    ctx.json(followersList);
                } catch (NotFoundException e) {
                    ctx.status(404).result(e.getMessage());
                } catch (SQLException e) {
                    ctx.status(500).result("Database error: " + e.getMessage());
                }
            }); // end of GET /users/{id}/followers



        }); // end of javalin config

        app.start(8080);

    } // end of main()
} // end of class