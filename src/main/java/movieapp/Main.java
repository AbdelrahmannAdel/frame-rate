package movieapp;

import movieapp.api.*;
import movieapp.db.*;
import movieapp.exception.*;
import movieapp.model.*;
import movieapp.service.*;
import movieapp.web.dto.*;

import io.javalin.Javalin;
import io.javalin.json.JavalinJackson;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        Javalin app = Javalin.create(config -> {

            // registers Jackson as Javalin's JSON serializer, with jackson-datatype-jsr310 on
            // the classpath so LocalDateTime fields serialize correctly
            // instead of throwing InvalidDefinitionException
            config.jsonMapper(new JavalinJackson());

            // ================ GET ROUTES ================

            config.routes.get("/", ctx ->
                    ctx.result("Hello from Javalin!")
            ); // end of GET /

            config.routes.get("/movies/{id}", ctx -> {
                int id = Integer.parseInt(ctx.pathParam("id"));

                try (Connection conn = DatabaseConfig.getConnection()) {
                    MovieService movieService = new MovieService(conn);
                    Movie movie = movieService.getMovieById(id);
                    ctx.json(movie);
                }
            }); // end of GET /movies/{id}

            config.routes.get("/movies", ctx -> {
               try (Connection conn = DatabaseConfig.getConnection()){
                   MovieService movieService = new MovieService(conn);
                   List<Movie> moviesLis = movieService.getAllMovies();
                   ctx.json(moviesLis);
               }
            }); // end of GET /movies

            config.routes.get("/movies/{id}/reviews", ctx -> {
                int id = Integer.parseInt(ctx.pathParam("id"));

                try (Connection conn = DatabaseConfig.getConnection()){
                    ReviewService reviewService = new ReviewService(conn);
                    List<Review> reviewsList = reviewService.getReviewsByMovie(id);
                    ctx.json(reviewsList);
                }
            }); // end of GET /movies/{id}/reviews

            config.routes.get("/users/{id}", ctx -> {
                int id = Integer.parseInt(ctx.pathParam("id"));

                try (Connection conn = DatabaseConfig.getConnection()) {
                    UserService userService = new UserService(conn);
                    ctx.json(userService.getUserById(id));
                }
            }); // end of get /users/{id}

            config.routes.get("/users/{id}/reviews", ctx -> {
                int id = Integer.parseInt(ctx.pathParam("id"));

                try (Connection conn = DatabaseConfig.getConnection()){
                    ReviewService reviewService = new ReviewService(conn);
                    List<Review> reviewsList = reviewService.getReviewsByUser(id);
                    ctx.json(reviewsList);
                }
            }); // end of GET /users/{id}/reviews

            config.routes.get("/users/{id}/watchlist", ctx -> {
                int id = Integer.parseInt(ctx.pathParam("id"));

                try (Connection conn = DatabaseConfig.getConnection()){
                    WatchlistService watchlistService = new WatchlistService(conn);
                    List<WatchlistEntry> watchList = watchlistService.getWatchlistByUser(id);
                    ctx.json(watchList);
                }
            }); // end of GET /users/{id}/watchlist

            config.routes.get("/users/{id}/following", ctx -> {
                int id = Integer.parseInt(ctx.pathParam("id"));

                try (Connection conn = DatabaseConfig.getConnection()){
                    FollowService followService = new FollowService(conn);
                    List<User> followingList = followService.getFollowing(id);
                    ctx.json(followingList);
                }
            }); // end of GET /users/{id}/following

            config.routes.get("/users/{id}/followers", ctx -> {
                int id = Integer.parseInt(ctx.pathParam("id"));

                try (Connection conn = DatabaseConfig.getConnection()){
                    FollowService followService = new FollowService(conn);
                    List<User> followersList = followService.getFollowers(id);
                    ctx.json(followersList);
                }
            }); // end of GET /users/{id}/followers

            // ================ POST ROUTES ================

            config.routes.post("/users", ctx -> {
                RegisterUserRequest request = ctx.bodyAsClass(RegisterUserRequest.class);

                // hashing the password should happen here before passing it to registerUser()

                try (Connection conn = DatabaseConfig.getConnection()) {
                    UserService userService = new UserService(conn);
                    User user = userService.registerUser(request.getUsername(), request.getEmail(), request.getPassword());
                    ctx.status(201).json(user);
                }
            }); // end of POST /users

            config.routes.post("/movies/{id}/reviews", ctx -> {
                int movieId = Integer.parseInt(ctx.pathParam("id"));
                CreateReviewRequest request = ctx.bodyAsClass(CreateReviewRequest.class);

                try (Connection conn = DatabaseConfig.getConnection()){
                    ReviewService reviewService = new ReviewService(conn);
                    Review review = reviewService.createReview(request.getUserId(), movieId, request.getRating());
                    ctx.status(201).json(review);
                }
            }); // end of POST /movies/{id}/reviews

            config.routes.post("/users/{id}/watchlist", ctx -> {
                int userId = Integer.parseInt(ctx.pathParam("id"));
                AddToWatchlistRequest request = ctx.bodyAsClass(AddToWatchlistRequest.class);

                try (Connection conn = DatabaseConfig.getConnection()){
                    WatchlistService watchlistService = new WatchlistService(conn);
                    WatchlistEntry watchlistEntry = watchlistService.addToWatchlist(userId, request.getMovieId());
                    ctx.status(201).json(watchlistEntry);
                }
            }); // end of POST /users/{id}/watchlist

            config.routes.post("/users/{id}/following", ctx -> {
                int followerId = Integer.parseInt(ctx.pathParam("id"));
                FollowUserRequest request = ctx.bodyAsClass(FollowUserRequest.class);

                try (Connection conn = DatabaseConfig.getConnection()) {
                    FollowService followService = new FollowService(conn);
                    Follow follow = followService.followUser(followerId, request.getFolloweeId());
                    ctx.status(201).json(follow);
                }
            }); // end of POST /users/{id}/following



            // ================ EXCEPTION ROUTES ================

            config.routes.exception(NotFoundException.class, (e, ctx) -> {ctx.status(404).result(e.getMessage());});
            config.routes.exception(DuplicateUsernameException.class, (e, ctx) -> {ctx.status(409).result(e.getMessage());});
            config.routes.exception(DuplicateEmailException.class, (e, ctx) -> {ctx.status(409).result(e.getMessage());});
            config.routes.exception(DuplicateMovieException.class, (e, ctx) -> {ctx.status(409).result(e.getMessage());});
            config.routes.exception(DuplicateReviewException.class, (e, ctx) -> {ctx.status(409).result(e.getMessage());});
            config.routes.exception(DuplicateWatchlistException.class, (e, ctx) -> {ctx.status(409).result(e.getMessage());});
            config.routes.exception(DuplicateFollowException.class, (e, ctx) -> {ctx.status(409).result(e.getMessage());});
            config.routes.exception(SelfFollowException.class, (e, ctx) -> {ctx.status(400).result(e.getMessage());});
            config.routes.exception(InvalidRatingException.class, (e, ctx) -> {ctx.status(400).result(e.getMessage());});
            config.routes.exception(UnauthorizedActionException.class, (e, ctx) -> {ctx.status(403).result(e.getMessage());});
            config.routes.exception(SQLException.class, (e, ctx) -> {ctx.status(500).result("Database error: " + e.getMessage());});

        }); // end of javalin config

        app.start(8080);

    } // end of main()
} // end of class