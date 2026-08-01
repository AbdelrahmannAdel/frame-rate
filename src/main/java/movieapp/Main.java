package movieapp;

import movieapp.db.*;
import movieapp.exception.*;
import movieapp.model.*;
import movieapp.service.*;
import movieapp.web.dto.request.*;
import movieapp.web.dto.response.*;

import io.javalin.Javalin;
import io.javalin.json.JavalinJackson;

import java.sql.Connection;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {

        Javalin app = Javalin.create(config -> {

            // registers Jackson as Javalin's JSON serializer, with jackson-datatype-jsr310 on
            // the classpath so LocalDateTime fields serialize correctly
            // instead of throwing InvalidDefinitionException
            config.jsonMapper(new JavalinJackson());

            // ================ GET ROUTES ================

            // GET /
            // Expects: no params | no body
            // Returns: 200 + plain text "Hello from Javalin!"
            config.routes.get("/", ctx ->
                    ctx.result("Hello from Javalin!")
            );

            // GET /movies
            // Expects: no params | no body
            // Returns: 200 +   [
            //                   {
            //                    "id":int,
            //                    "tmdbId":int,
            //                    "title":string,
            //                    "releaseYear":int|null,
            //                    "posterPath":string|null,
            //                    "overview":string|null,
            //                    "runtimeMinutes":int|null,
            //                    "cachedAt":datetime
            //                   }
            //                  ]
            config.routes.get("/movies", ctx -> {
                try (Connection conn = DatabaseConfig.getConnection()){
                    MovieService movieService = new MovieService(conn);
                    List<Movie> moviesLis = movieService.getAllMovies();
                    ctx.json(moviesLis);
                }
            });

            // GET /movies/{id}
            // Expects: path -> movie id
            // Returns: 200 +   {
            //                    "id":int,
            //                    "tmdbId":int,
            //                    "title":string,
            //                    "releaseYear":int|null,
            //                    "posterPath":string|null,
            //                    "overview":string|null,
            //                    "runtimeMinutes":int|null,
            //                    "cachedAt":datetime
            //                   }
            config.routes.get("/movies/{id}", ctx -> {
                int id = Integer.parseInt(ctx.pathParam("id"));

                try (Connection conn = DatabaseConfig.getConnection()) {
                    MovieService movieService = new MovieService(conn);
                    Movie movie = movieService.getMovieById(id);
                    ctx.json(movie);
                }
            });

            // GET /movies/{id}/reviews
            // Expects: path -> movie id
            // Returns: 200 +   [
            //                   {
            //                    "id":int,
            //                    "userId":int,
            //                    "movieId":int,
            //                    "rating":int,
            //                    "createdAt":datetime
            //                   }
            //                  ]
            config.routes.get("/movies/{id}/reviews", ctx -> {
                int id = Integer.parseInt(ctx.pathParam("id"));

                try (Connection conn = DatabaseConfig.getConnection()){
                    ReviewService reviewService = new ReviewService(conn);
                    List<Review> reviewsList = reviewService.getReviewsByMovie(id);
                    ctx.json(reviewsList);
                }
            });

            // GET /users/{id}
            // Expects: path -> user id
            // Returns: 200 +   {
            //                   "id":int,
            //                   "username":string,
            //                   "email":string,
            //                   "createdAt":datetime
            //                  }
            config.routes.get("/users/{id}", ctx -> {
                int id = Integer.parseInt(ctx.pathParam("id"));

                try (Connection conn = DatabaseConfig.getConnection()) {
                    UserService userService = new UserService(conn);
                    UserResponse userResponse = new UserResponse(userService.getUserById(id));
                    ctx.json(userResponse);
                }
            });


            // GET /users/{id}/reviews
            // Expects: path -> user id
            // Returns: 200 +   [
            //                   {
            //                    "id":int,
            //                    "userId":int,
            //                    "movieId":int,
            //                    "rating":int,
            //                    "createdAt":datetime
            //                   }
            //                  ]
            config.routes.get("/users/{id}/reviews", ctx -> {
                int id = Integer.parseInt(ctx.pathParam("id"));

                try (Connection conn = DatabaseConfig.getConnection()){
                    ReviewService reviewService = new ReviewService(conn);
                    List<Review> reviewsList = reviewService.getReviewsByUser(id);
                    ctx.json(reviewsList);
                }
            });

            // GET /users/{id}/watchlist
            // Expects: path -> user id
            // Returns: 200 +   [
            //                   {
            //                    "id":int,
            //                    "userId":int,
            //                    "movieId":int,
            //                    "addedAt":datetime
            //                   }
            //                  ]
            config.routes.get("/users/{id}/watchlist", ctx -> {
                int id = Integer.parseInt(ctx.pathParam("id"));

                try (Connection conn = DatabaseConfig.getConnection()){
                    WatchlistService watchlistService = new WatchlistService(conn);
                    List<WatchlistEntry> watchList = watchlistService.getWatchlistByUser(id);
                    ctx.json(watchList);
                }
            });

            // GET /users/{id}/following
            // Expects: path -> user id
            // Returns: 200 +   [
            //                   {
            //                    "id":int,
            //                    "username":string,
            //                    "email":string,
            //                    "createdAt":datetime
            //                   }
            //                  ]
            config.routes.get("/users/{id}/following", ctx -> {
                int id = Integer.parseInt(ctx.pathParam("id"));

                try (Connection conn = DatabaseConfig.getConnection()){
                    FollowService followService = new FollowService(conn);
                    List<User> followingList = followService.getFollowing(id);

                    List<UserResponse> responseList = new ArrayList<>();
                    for (User user : followingList) {
                        responseList.add(new UserResponse(user));
                    }

                    ctx.json(responseList);
                }
            });

            // GET /users/{id}/followers
            // Expects: path -> user id
            // Returns: 200 +   [
            //                   {
            //                    "id":int,
            //                    "username":string,
            //                    "email":string,
            //                    "createdAt":datetime
            //                   }
            //                  ]
            config.routes.get("/users/{id}/followers", ctx -> {
                int id = Integer.parseInt(ctx.pathParam("id"));

                try (Connection conn = DatabaseConfig.getConnection()){
                    FollowService followService = new FollowService(conn);
                    List<User> followersList = followService.getFollowers(id);

                    List<UserResponse> responseList = new ArrayList<>();
                    for (User user : followersList) {
                        responseList.add(new UserResponse(user));
                    }

                    ctx.json(responseList);
                }
            });

            // ================ POST ROUTES ================

            // POST /users
            // Expects: body -> {
            //                   "username":string,
            //                   "email":string,
            //                   "password":string
            //                  }
            // Returns: 201 + {
            //                 "id":int,
            //                 "username":string,
            //                 "email":string,
            //                 "createdAt":datetime
            //                }
            // Throws: DuplicateUsernameException (409), DuplicateEmailException (409)
            config.routes.post("/users", ctx -> {
                RegisterUserRequest request = ctx.bodyAsClass(RegisterUserRequest.class);

                // hashing the password should happen here before passing it to registerUser()

                try (Connection conn = DatabaseConfig.getConnection()) {
                    UserService userService = new UserService(conn);
                    User user = userService.registerUser(request.getUsername(), request.getEmail(), request.getPassword());
                    UserResponse userResponse = new UserResponse(user);
                    ctx.status(201).json(userResponse);
                }
            });

            // POST /movies/{id}/reviews
            // Expects: path -> movie id
            //          body -> {
            //                   "userId":int,
            //                   "rating":int
            //                  }
            // Returns: 201 + {
            //                 "id":int,
            //                 "userId":int,
            //                 "movieId":int,
            //                 "rating":int,
            //                 "createdAt":datetime
            //                }
            // Throws: InvalidRatingException (400), DuplicateReviewException (409)
            config.routes.post("/movies/{id}/reviews", ctx -> {
                int movieId = Integer.parseInt(ctx.pathParam("id"));
                CreateReviewRequest request = ctx.bodyAsClass(CreateReviewRequest.class);

                try (Connection conn = DatabaseConfig.getConnection()){
                    ReviewService reviewService = new ReviewService(conn);
                    Review review = reviewService.createReview(request.getUserId(), movieId, request.getRating());
                    ctx.status(201).json(review);
                }
            });

            // POST /users/{id}/watchlist
            // Expects: path -> user id
            //          body -> {"movieId":int}
            // Returns: 201 + {
            //                 "id":int,
            //                 "userId":int,
            //                 "movieId":int,
            //                 "addedAt":datetime
            //                }
            // Throws: DuplicateWatchlistException (409)
            config.routes.post("/users/{id}/watchlist", ctx -> {
                int userId = Integer.parseInt(ctx.pathParam("id"));
                AddToWatchlistRequest request = ctx.bodyAsClass(AddToWatchlistRequest.class);

                try (Connection conn = DatabaseConfig.getConnection()){
                    WatchlistService watchlistService = new WatchlistService(conn);
                    WatchlistEntry watchlistEntry = watchlistService.addToWatchlist(userId, request.getMovieId());
                    ctx.status(201).json(watchlistEntry);
                }
            });

            // POST /users/{id}/following
            // Expects: path -> user id (the follower)
            //          body -> {"followeeId":int}
            // Returns: 201 + {
            //                 "followerId":int,
            //                 "followeeId":int,
            //                 "createdAt":datetime
            //                }
            // Throws: SelfFollowException (400), DuplicateFollowException (409)
            config.routes.post("/users/{id}/following", ctx -> {
                int followerId = Integer.parseInt(ctx.pathParam("id"));
                FollowUserRequest request = ctx.bodyAsClass(FollowUserRequest.class);

                try (Connection conn = DatabaseConfig.getConnection()) {
                    FollowService followService = new FollowService(conn);
                    Follow follow = followService.followUser(followerId, request.getFolloweeId());
                    ctx.status(201).json(follow);
                }
            });

            // ================ PUT ROUTES ================

            // PUT /movies/{id}/reviews/{reviewId}
            // Expects: path -> movie id, review id
            //          query -> userId
            //          body -> {"rating":int}
            // Returns: 200 + {
            //                 "id":int,
            //                 "userId":int,
            //                 "movieId":int,
            //                 "rating":int,
            //                 "createdAt":datetime
            //                }
            // Throws: NotFoundException (404), UnauthorizedActionException (403), InvalidRatingException (400)
            config.routes.put("/movies/{id}/reviews/{reviewId}", ctx -> {
                int reviewId = Integer.parseInt(ctx.pathParam("reviewId"));
                int userId = Integer.parseInt(ctx.queryParam("userId"));
                UpdateReviewRequest request = ctx.bodyAsClass(UpdateReviewRequest.class);

                try (Connection conn = DatabaseConfig.getConnection()){
                    ReviewService reviewService = new ReviewService(conn);
                    Review updatedReview = reviewService.updateReview(userId,reviewId, request.getRating());
                    ctx.status(200).json(updatedReview);
                }
            });

            // PUT /users/{id}/username
            // Expects: path -> user id
            //          body -> {"username":string}
            // Returns: 200 + {
            //                 "id":int,
            //                 "username":string,
            //                 "email":string,
            //                 "createdAt":datetime
            //                }
            // Throws: NotFoundException (404), DuplicateUsernameException (409)
            config.routes.put("/users/{id}/username", ctx -> {
                int userId = Integer.parseInt(ctx.pathParam("id"));
                UpdateUsernameRequest request = ctx.bodyAsClass(UpdateUsernameRequest.class);

                try (Connection conn = DatabaseConfig.getConnection()){
                    UserService userService = new UserService(conn);
                    User user = userService.updateUsername(userId, request.getUsername());
                    UserResponse userResponse = new UserResponse(user);
                    ctx.status(200).json(userResponse);
                }
            });

            // PUT /users/{id}/email
            // Expects: path -> user id
            //          body -> {"email":string}
            // Returns: 200 + {
            //                 "id":int,
            //                 "username":string,
            //                 "email":string,
            //                 "createdAt":datetime
            //                }
            // Throws: NotFoundException (404), DuplicateEmailException (409)
            config.routes.put("/users/{id}/email", ctx -> {
                int userId = Integer.parseInt(ctx.pathParam("id"));
                UpdateEmailRequest request = ctx.bodyAsClass(UpdateEmailRequest.class);

                try (Connection conn = DatabaseConfig.getConnection()){
                    UserService userService = new UserService(conn);
                    User updatedUser = userService.updateEmail(userId, request.getEmail());
                    UserResponse userResponse = new UserResponse(updatedUser);
                    ctx.status(200).json(userResponse);
                }
            });

            // PUT /users/{id}/password
            // Expects: path -> user id
            //          body -> {"password":string}
            // Returns: 200 + {
            //                 "id":int,
            //                 "username":string,
            //                 "email":string,
            //                 "createdAt":datetime
            //                }
            // Throws: NotFoundException (404)
            config.routes.put("/users/{id}/password", ctx -> {
                int userId = Integer.parseInt(ctx.pathParam("id"));
                UpdatePasswordRequest request = ctx.bodyAsClass(UpdatePasswordRequest.class);

                // hashing the password should happen here before passing it to updatePassword()

                try (Connection conn = DatabaseConfig.getConnection()){
                    UserService userService = new UserService(conn);
                    User updatedUser = userService.updatePassword(userId, request.getPassword());
                    UserResponse userResponse = new UserResponse(updatedUser);
                    ctx.status(200).json(userResponse);
                }
            });

            // ================ DELETE ROUTES ================

            // DELETE /movies/{id}
            // Expects: path -> movie id
            // Returns: 200 + {"deleted":true}
            // Throws: NotFoundException (404)
            config.routes.delete("/movies/{id}", ctx -> {
                int movieId = Integer.parseInt(ctx.pathParam("id"));

                try (Connection conn = DatabaseConfig.getConnection()) {
                    MovieService movieService = new MovieService(conn);
                    movieService.deleteMovie(movieId);
                    ctx.status(200).json(Map.of("deleted", true));
                }
            });

            // DELETE /users/{id}
            // Expects: path -> user id
            // Returns: 200 + {"deleted":true}
            // Throws: NotFoundException (404)
            config.routes.delete("/users/{id}", ctx -> {
                int userId = Integer.parseInt(ctx.pathParam("id"));
                try (Connection conn = DatabaseConfig.getConnection()) {
                    UserService userService = new UserService(conn);
                    userService.deleteUser(userId);
                    ctx.status(200).json(Map.of("deleted", true));
                }
            });

            // DELETE /users/{id}/watchlist/{entryId}
            // Expects: path -> user id, watchlist entry id
            // Returns: 200 + {"deleted":true}
            // Throws: NotFoundException (404), UnauthorizedActionException (403)
            config.routes.delete("/users/{id}/watchlist/{entryId}", ctx -> {
                int userId = Integer.parseInt(ctx.pathParam("id"));
                int entryId = Integer.parseInt(ctx.pathParam("entryId"));

                try (Connection conn = DatabaseConfig.getConnection()) {
                    WatchlistService watchlistService = new WatchlistService(conn);
                    watchlistService.removeFromWatchlist(userId, entryId);
                    ctx.status(200).json(Map.of("deleted", true));
                }
            });

            // DELETE /users/{id}/following/{followeeId}
            // Expects: path -> user id (the follower), followee id
            // Returns: 200 + {"deleted":true}
            // Throws: NotFoundException (404)
            config.routes.delete("/users/{id}/following/{followeeId}", ctx -> {
                int userId = Integer.parseInt(ctx.pathParam("id"));
                int followeeId = Integer.parseInt(ctx.pathParam("followeeId"));

                try (Connection conn = DatabaseConfig.getConnection()) {
                    FollowService followService = new FollowService(conn);
                    followService.unfollowUser(userId, followeeId);
                    ctx.status(200).json(Map.of("deleted", true));
                }
            });

            // DELETE /movies/{id}/reviews/{reviewId}
            // Expects: path -> movie id, review id
            //          query -> userId
            // Returns: 200 + {"deleted":true}
            // Throws: NotFoundException (404), UnauthorizedActionException (403)
            config.routes.delete("/movies/{id}/reviews/{reviewId}", ctx -> {
                int reviewId = Integer.parseInt(ctx.pathParam("reviewId"));
                int userId = Integer.parseInt(ctx.queryParam("userId"));

                try (Connection conn = DatabaseConfig.getConnection()) {
                    ReviewService reviewService = new ReviewService(conn);
                    reviewService.deleteReview(userId, reviewId);
                    ctx.status(200).json(Map.of("deleted", true));
                }
            });

            // ================ EXCEPTION ROUTES ================

            config.routes.exception(NotFoundException.class, (e, ctx) -> ctx.status(404).result(e.getMessage()));
            config.routes.exception(DuplicateUsernameException.class, (e, ctx) -> ctx.status(409).result(e.getMessage()));
            config.routes.exception(DuplicateEmailException.class, (e, ctx) -> ctx.status(409).result(e.getMessage()));
            config.routes.exception(DuplicateMovieException.class, (e, ctx) -> ctx.status(409).result(e.getMessage()));
            config.routes.exception(DuplicateReviewException.class, (e, ctx) -> ctx.status(409).result(e.getMessage()));
            config.routes.exception(DuplicateWatchlistException.class, (e, ctx) -> ctx.status(409).result(e.getMessage()));
            config.routes.exception(DuplicateFollowException.class, (e, ctx) -> ctx.status(409).result(e.getMessage()));
            config.routes.exception(SelfFollowException.class, (e, ctx) -> ctx.status(400).result(e.getMessage()));
            config.routes.exception(InvalidRatingException.class, (e, ctx) -> ctx.status(400).result(e.getMessage()));
            config.routes.exception(UnauthorizedActionException.class, (e, ctx) -> ctx.status(403).result(e.getMessage()));
            config.routes.exception(SQLException.class, (e, ctx) -> ctx.status(500).result("Database error: " + e.getMessage()));

        }); // end of javalin config

        app.start(8080);

    } // end of main()
} // end of class