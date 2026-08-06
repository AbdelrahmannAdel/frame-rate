package movieapp;

import movieapp.api.*;
import movieapp.api.dto.*;
import movieapp.auth.*;
import movieapp.db.*;
import movieapp.exception.*;
import movieapp.model.*;
import movieapp.service.*;
import movieapp.web.dto.request.*;
import movieapp.web.dto.response.*;

import io.javalin.Javalin;
import io.javalin.json.JavalinJackson;
import io.javalin.http.HandlerType;
import io.javalin.http.UnauthorizedResponse;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

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

            // enables CORS so the React dev server (localhost:5173) can call this API.
            // without this, the browser blocks the frontend from reading responses,
            // even though the request actually reaches and is handled by this server.
            config.bundledPlugins.enableCors(cors -> {
                cors.addRule(rule -> {
                    rule.allowHost("http://localhost:5173");
                });
            });

            // global before-handler: runs before every request.
            // GET requests (except /users/compatibility), POST /users, and POST /login are public,
            // everything else requires a valid Bearer token, and stores the verified userId on ctx for routes to use.
            config.routes.before(ctx -> {

                ctx.header("Access-Control-Allow-Origin", "http://localhost:5173");

                // preflight requests (OPTIONS) never carry an Authorization header.
                // browsers send them automatically before certain cross-origin requests,
                // and they must be allowed through so the CORS plugin can respond to them
                if (ctx.method() == HandlerType.OPTIONS) return;

                // compatibility is a GET route, but unlike every other GET, it needs to know
                // who is asking (the token supplies userId) so it's a deliberate exception
                // to the "all GETs are public" rule
                boolean isCompatibilityRoute = ctx.path().startsWith("/users/compatibility/");

                // allow these through without a token
                if (ctx.method() == HandlerType.GET && !isCompatibilityRoute) return;
                if (ctx.path().equals("/users") && ctx.method() == HandlerType.POST) return;
                if (ctx.path().equals("/login")) return;

                // everything else requires a valid token

                // reads the Authorization HTTP header
                // format of header is:
                // Authorization: Bearer <Token>
                String authHeader = ctx.header("Authorization");

                // if no authorization header or it doesn't match 'Authorization: Bearer'
                if (authHeader == null || !authHeader.startsWith("Bearer "))
                    throw new UnauthorizedResponse("Missing or invalid Authorization header");

                String token = authHeader.substring(7);           // strip "Bearer " prefix
                DecodedJWT decodedJWT = JwtService.verifyToken(token);      // verify the token
                int userId = decodedJWT.getClaim("userId").asInt();   // extract userId from the token

                // store the verified userId on the context object
                ctx.attribute("userId", userId);
            });

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

            // GET /movies/search?title=...
            // Expects: query param -> title
            // Returns: 200 +   [
            //                   {
            //                    "tmdbId":int,
            //                    "title":string,
            //                    "overview":string,
            //                    "releaseDate":string,
            //                    "posterPath":string|null
            //                   }
            //                  ]
            // Throws: IOException (502, if TMDB call fails)
            config.routes.get("/movies/search", ctx -> {
                String title = ctx.queryParam("title");

                TmdbClient tmdbClient = new TmdbClient();
                String json = tmdbClient.searchMovies(title);
                List<TmdbMovieResult> results = tmdbClient.parseSearchResults(json);

                ctx.json(results);
            });

            // GET /movies/top-rated
            // Expects: no params | no body
            // Returns: 200 +   [
            //                   {
            //                    "movie": { "id":int, "tmdbId":int, "title":string, ... },
            //                    "averageRating": double
            //                   }
            //                  ]
            config.routes.get("/movies/top-rated", ctx -> {
                try (Connection conn = DatabaseConfig.getConnection()) {
                    MovieService movieService = new MovieService(conn);
                    List<RatedMovie> topRated = movieService.getTopRatedMovies();
                    ctx.json(topRated);
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

            // GET /users/compatibility/{otherId}
            // Expects: path -> the other user's id
            // Returns: 200 + {
            //                 "compatibilityScore": double|null,
            //                 "sharedMovies": [
            //                   { "movieId":int, "title":string, "myRating":int, "theirRating":int }
            //                 ]
            //                }
            // Throws: NotFoundException (404), NotMutualFollowException (403)
            config.routes.get("/users/compatibility/{otherId}", ctx -> {
                int userId = (int) ctx.attribute("userId");
                int otherId = Integer.parseInt(ctx.pathParam("otherId"));

                try (Connection conn = DatabaseConfig.getConnection()) {
                    CompatibilityService compatibilityService = new CompatibilityService(conn);
                    CompatibilityResult result = compatibilityService.getCompatibility(userId, otherId);
                    ctx.json(result);
                }
            });

            // ================ POST ROUTES ================

            // POST /login
            // Expects: body -> {"email":string,"password":string}
            // Returns: 200 + {"token":string}
            // Throws: InvalidCredentialsException (401)
            config.routes.post("/login", ctx -> {
                LoginRequest request = ctx.bodyAsClass(LoginRequest.class);

                try (Connection conn = DatabaseConfig.getConnection()) {
                    UserService userService = new UserService(conn);
                    User user = userService.login(request.getEmail(), request.getPassword());
                    String token = JwtService.generateToken(user.getId());
                    ctx.status(200).json(Map.of("token", token));
                }
            });

            // POST /users - (Register)
            // Expects: body -> {
            //                   "username":string,
            //                   "email":string,
            //                   "password":string
            //                  }
            // Returns: 201 + {
            //                 "user":
            //                        {
            //                         "id":int,
            //                         "username":string,
            //                         "email":string,
            //                         "createdAt":datetime
            //                        },
            //                 "token": string
            //                }
            // Throws: DuplicateUsernameException (409), DuplicateEmailException (409)
            config.routes.post("/users", ctx -> {
                RegisterUserRequest request = ctx.bodyAsClass(RegisterUserRequest.class);

                // hash password first
                String hashedPassword = PasswordHasher.hash(request.getPassword());

                try (Connection conn = DatabaseConfig.getConnection()) {
                    UserService userService = new UserService(conn);
                    User user = userService.registerUser(request.getUsername(), request.getEmail(), hashedPassword);
                    UserResponse userResponse = new UserResponse(user);
                    String token = JwtService.generateToken(user.getId());
                    ctx.status(201).json(Map.of("user", userResponse, "token", token));
                }
            });

            // POST /users/watchlist
            // Expects: body -> {"movieId":int}
            // Returns: 201 + {
            //                 "id":int,
            //                 "userId":int,
            //                 "movieId":int,
            //                 "addedAt":datetime
            //                }
            // Throws: DuplicateWatchlistException (409)
            config.routes.post("/users/watchlist", ctx -> {
                int userId = (int) ctx.attribute("userId");
                AddToWatchlistRequest request = ctx.bodyAsClass(AddToWatchlistRequest.class);

                try (Connection conn = DatabaseConfig.getConnection()){
                    WatchlistService watchlistService = new WatchlistService(conn);
                    WatchlistEntry watchlistEntry = watchlistService.addToWatchlist(userId, request.getMovieId());
                    ctx.status(201).json(watchlistEntry);
                }
            });

            // POST /users/following
            // Expects: body -> {"followeeId":int}
            // Returns: 201 + {
            //                 "followerId":int,
            //                 "followeeId":int,
            //                 "createdAt":datetime
            //                }
            // Throws: SelfFollowException (400), DuplicateFollowException (409)
            config.routes.post("/users/following", ctx -> {
                int userId = (int) ctx.attribute("userId");
                FollowUserRequest request = ctx.bodyAsClass(FollowUserRequest.class);

                try (Connection conn = DatabaseConfig.getConnection()) {
                    FollowService followService = new FollowService(conn);
                    Follow follow = followService.followUser(userId, request.getFolloweeId());
                    ctx.status(201).json(follow);
                }
            });

            // POST /movies/import
            // Expects: body -> {"tmdbId":int}
            // Returns: 201 + {
            //                 "id":int,
            //                 "tmdbId":int,
            //                 "title":string,
            //                 "releaseYear":int|null,
            //                 "posterPath":string|null,
            //                 "overview":string|null,
            //                 "runtimeMinutes":int|null,
            //                 "cachedAt":datetime
            //                }
            // Throws: DuplicateMovieException (409, race-condition edge case), IOException (502)
            config.routes.post("/movies/import", ctx -> {
                ImportMovieRequest request = ctx.bodyAsClass(ImportMovieRequest.class);

                try (Connection conn = DatabaseConfig.getConnection()) {
                    MovieService movieService = new MovieService(conn);
                    Movie movie = movieService.importByTmdbId(request.getTmdbId());
                    ctx.status(201).json(movie);
                }
            });

            // POST /movies/{id}/reviews
            // Expects: path -> movie id
            //          body -> {"rating":int}
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
                int userId = (int) ctx.attribute("userId");
                CreateReviewRequest request = ctx.bodyAsClass(CreateReviewRequest.class);

                try (Connection conn = DatabaseConfig.getConnection()){
                    ReviewService reviewService = new ReviewService(conn);
                    Review review = reviewService.createReview(userId, movieId, request.getRating());
                    ctx.status(201).json(review);
                }
            });

            // ================ PUT ROUTES ================

            // PUT /movies/{id}/reviews/{reviewId}
            // Expects: path -> movie id, review id
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
                int userId = (int) ctx.attribute("userId");
                UpdateReviewRequest request = ctx.bodyAsClass(UpdateReviewRequest.class);

                try (Connection conn = DatabaseConfig.getConnection()){
                    ReviewService reviewService = new ReviewService(conn);
                    Review updatedReview = reviewService.updateReview(userId,reviewId, request.getRating());
                    ctx.status(200).json(updatedReview);
                }
            });

            // PUT /users/username
            // Expects: body -> {"username":string}
            // Returns: 200 + {
            //                 "id":int,
            //                 "username":string,
            //                 "email":string,
            //                 "createdAt":datetime
            //                }
            // Throws: NotFoundException (404), DuplicateUsernameException (409)
            config.routes.put("/users/username", ctx -> {
                int userId = (int) ctx.attribute("userId");
                UpdateUsernameRequest request = ctx.bodyAsClass(UpdateUsernameRequest.class);

                try (Connection conn = DatabaseConfig.getConnection()){
                    UserService userService = new UserService(conn);
                    User user = userService.updateUsername(userId, request.getUsername());
                    UserResponse userResponse = new UserResponse(user);
                    ctx.status(200).json(userResponse);
                }
            });

            // PUT /users/email
            // Expects: body -> {"email":string}
            // Returns: 200 + {
            //                 "id":int,
            //                 "username":string,
            //                 "email":string,
            //                 "createdAt":datetime
            //                }
            // Throws: NotFoundException (404), DuplicateEmailException (409)
            config.routes.put("/users/email", ctx -> {
                int userId = (int) ctx.attribute("userId");
                UpdateEmailRequest request = ctx.bodyAsClass(UpdateEmailRequest.class);

                try (Connection conn = DatabaseConfig.getConnection()){
                    UserService userService = new UserService(conn);
                    User updatedUser = userService.updateEmail(userId, request.getEmail());
                    UserResponse userResponse = new UserResponse(updatedUser);
                    ctx.status(200).json(userResponse);
                }
            });

            // PUT /users/password
            // Expects: body -> {"password":string}
            // Returns: 200 + {
            //                 "id":int,
            //                 "username":string,
            //                 "email":string,
            //                 "createdAt":datetime
            //                }
            // Throws: NotFoundException (404)
            config.routes.put("/users/password", ctx -> {
                int userId = (int) ctx.attribute("userId");
                UpdatePasswordRequest request = ctx.bodyAsClass(UpdatePasswordRequest.class);

                // hashing the password first
                String hashedPassword =  PasswordHasher.hash(request.getPassword());

                try (Connection conn = DatabaseConfig.getConnection()){
                    UserService userService = new UserService(conn);
                    User updatedUser = userService.updatePassword(userId, hashedPassword);
                    UserResponse userResponse = new UserResponse(updatedUser);
                    ctx.status(200).json(userResponse);
                }
            });

            // ================ DELETE ROUTES ================

            // DELETE /movies/{id} - commented out intentionally.
            // Expects: path -> movie id
            // Returns: 200 + {"deleted":true}
            // Throws: NotFoundException (404)

            // any logged-in user could currently delete any movie (cascading to its reviews
            // and watchlist entries too), since there's no admin/role system yet to restrict
            // this to privileged users.
            // re-enable once role-based authorization exists

            // config.routes.delete("/movies/{id}", ctx -> {
            //     int movieId = Integer.parseInt(ctx.pathParam("id"));
            //
            //     try (Connection conn = DatabaseConfig.getConnection()) {
            //         MovieService movieService = new MovieService(conn);
            //         movieService.deleteMovie(movieId);
            //         ctx.status(200).json(Map.of("deleted", true));
            //     }
            // });

            // DELETE /users
            // Expects: no params | no body
            // Returns: 200 + {"deleted":true}
            // Throws: NotFoundException (404)
            config.routes.delete("/users", ctx -> {
                int userId = (int) ctx.attribute("userId");
                try (Connection conn = DatabaseConfig.getConnection()) {
                    UserService userService = new UserService(conn);
                    userService.deleteUser(userId);
                    ctx.status(200).json(Map.of("deleted", true));
                }
            });

            // DELETE /users/watchlist/{entryId}
            // Expects: path -> watchlist entryId
            // Returns: 200 + {"deleted":true}
            // Throws: NotFoundException (404), UnauthorizedActionException (403)
            config.routes.delete("/users/watchlist/{entryId}", ctx -> {
                int userId = (int) ctx.attribute("userId");
                int entryId = Integer.parseInt(ctx.pathParam("entryId"));

                try (Connection conn = DatabaseConfig.getConnection()) {
                    WatchlistService watchlistService = new WatchlistService(conn);
                    watchlistService.removeFromWatchlist(userId, entryId);
                    ctx.status(200).json(Map.of("deleted", true));
                }
            });

            // DELETE /users/following/{followeeId}
            // Expects: path -> followee id
            // Returns: 200 + {"deleted":true}
            // Throws: NotFoundException (404)
            config.routes.delete("/users/following/{followeeId}", ctx -> {
                int userId = (int) ctx.attribute("userId");
                int followeeId = Integer.parseInt(ctx.pathParam("followeeId"));

                try (Connection conn = DatabaseConfig.getConnection()) {
                    FollowService followService = new FollowService(conn);
                    followService.unfollowUser(userId, followeeId);
                    ctx.status(200).json(Map.of("deleted", true));
                }
            });

            // DELETE /movies/{id}/reviews/{reviewId}
            // Expects: path -> movie id, review id
            // Returns: 200 + {"deleted":true}
            // Throws: NotFoundException (404), UnauthorizedActionException (403)
            config.routes.delete("/movies/{id}/reviews/{reviewId}", ctx -> {
                int reviewId = Integer.parseInt(ctx.pathParam("reviewId"));
                int userId = (int) ctx.attribute("userId");

                try (Connection conn = DatabaseConfig.getConnection()) {
                    ReviewService reviewService = new ReviewService(conn);
                    reviewService.deleteReview(userId, reviewId);
                    ctx.status(200).json(Map.of("deleted", true));
                }
            });

            // ================ EXCEPTIONS ================

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
            config.routes.exception(NumberFormatException.class, (e, ctx) -> ctx.status(400).result("Invalid number format: " + e.getMessage()));
            config.routes.exception(InvalidCredentialsException.class, (e, ctx) -> ctx.status(401).result(e.getMessage()));
            config.routes.exception(JWTVerificationException.class, (e, ctx) -> ctx.status(401).result("Invalid or expired token"));
            config.routes.exception(java.io.IOException.class, (e, ctx) -> ctx.status(502).result("Failed to reach TMDB: " + e.getMessage()));
            config.routes.exception(InterruptedException.class, (e, ctx) -> ctx.status(502).result("Request to TMDB was interrupted: " + e.getMessage()));
            config.routes.exception(NotMutualFollowException.class, (e, ctx) -> ctx.status(403).result(e.getMessage()));

        }); // end of javalin config

        app.start(8080);

    } // end of main()
} // end of class