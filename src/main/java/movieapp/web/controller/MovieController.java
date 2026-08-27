package movieapp.web.controller;

import movieapp.exception.*;

import movieapp.model.Movie;
import movieapp.model.RatedMovie;
import movieapp.model.Review;
import movieapp.model.User;

import movieapp.repository.UserRepository;
import movieapp.service.MovieService;
import movieapp.service.ReviewService;
import movieapp.web.dto.request.CreateReviewRequest;
import movieapp.web.dto.request.ImportMovieRequest;
import movieapp.web.dto.request.UpdateReviewRequest;
import movieapp.web.dto.response.ReviewResponse;
import movieapp.api.TmdbClient;
import movieapp.api.dto.TmdbMovieResult;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
public class MovieController {

    private final UserRepository userRepository;
    private final ReviewService reviewService;
    private final MovieService movieService;
    private final TmdbClient tmdbClient;

    public MovieController(UserRepository userRepository,
                           ReviewService reviewService,
                           MovieService movieService,
                           TmdbClient tmdbClient) {
        this.userRepository = userRepository;
        this.reviewService = reviewService;
        this.movieService = movieService;
        this.tmdbClient = tmdbClient;
    }

    // ================ GET ================

    @GetMapping("/movies")
    public ResponseEntity<List<Movie>> getAllMovies() {
        List<Movie> moviesList = movieService.getAllMovies();
        return new ResponseEntity<>(moviesList, HttpStatus.OK);
    } // end of getAllMovies()

    @GetMapping("/movies/{id}")
    public ResponseEntity<Movie> getMovie(@PathVariable int id) throws NotFoundException {
        Movie movie = movieService.getMovieById(id);
        return new ResponseEntity<>(movie, HttpStatus.OK);
    } // end of getMovie()

    @GetMapping("/movies/{id}/reviews")
    public ResponseEntity<List<ReviewResponse>> getReviews(@PathVariable int id) throws NotFoundException {
        List<ReviewResponse> responseList = reviewService.getReviewsByMovie(id).stream()
                .map(review -> {
                    User reviewer = userRepository.findById(review.getUser().getId())
                            .orElseThrow(() -> new IllegalStateException(
                                    "Review " + review.getId() + " refers to a missing user"));
                    return new ReviewResponse(review, reviewer.getUsername());
                })
                .toList();

        return ResponseEntity.ok(responseList);
    } // end of getReviews()

    @GetMapping("/movies/search")
    public ResponseEntity<List<TmdbMovieResult>> searchMovies(@RequestParam String title)
            throws IOException, InterruptedException {

        String json = tmdbClient.searchMovies(title);
        List<TmdbMovieResult> results = tmdbClient.parseSearchResults(json);

        return ResponseEntity.ok(results);
    } // end of searchMovies()

    @GetMapping("/movies/top-rated")
    public ResponseEntity<List<RatedMovie>> getTopRatedMovies() {
        List<RatedMovie> moviesList = movieService.getTopRatedMovies();
        return new ResponseEntity<>(moviesList, HttpStatus.OK);
    } // end of getTopRatedMovies()

    // ================ POST ================

    @PostMapping("/movies/import")
    public ResponseEntity<Movie>  importMovie(
            @AuthenticationPrincipal Integer userId,
            @RequestBody ImportMovieRequest request)
            throws IOException, InterruptedException, DuplicateMovieException {

        Movie movie = movieService.importByTmdbId(request.tmdbId());
        return new  ResponseEntity<>(movie, HttpStatus.CREATED);
    } // end of importMovie

    @PostMapping("/movies/{id}/reviews")
    public ResponseEntity<ReviewResponse> addReview(
            @AuthenticationPrincipal Integer userId,
            @PathVariable int id,
            @RequestBody CreateReviewRequest request)
            throws DuplicateReviewException, NotFoundException, InvalidRatingException {

        Review review = reviewService.createReview(userId, id, request.rating());
        User reviewer = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("No user found with id: " + userId));

        ReviewResponse reviewResponse = new ReviewResponse(review, reviewer.getUsername());
        return new ResponseEntity<>(reviewResponse, HttpStatus.CREATED);
    } // end of addReview()

    // ================ PUT ================

    @PutMapping("/movies/{id}/reviews/{reviewId}")
    public ResponseEntity<ReviewResponse> updateReview(
            @AuthenticationPrincipal Integer userId,
            @PathVariable int id,
            @PathVariable int reviewId,
            @RequestBody UpdateReviewRequest request)
            throws NotFoundException, UnauthorizedActionException, InvalidRatingException {

        Review review = reviewService.updateReview(userId, reviewId, request.rating());
        User reviewer = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("No user found with id: " + userId));

        ReviewResponse reviewResponse = new ReviewResponse(review, reviewer.getUsername());
        return new ResponseEntity<>(reviewResponse, HttpStatus.OK);
    } // end of updateReview()

    // ================ DELETE ================

    @DeleteMapping("/movies/{id}/reviews/{reviewId}")
    public ResponseEntity<Map<String, Boolean>> deleteReview(
            @AuthenticationPrincipal Integer userId,
            @PathVariable int id,
            @PathVariable int reviewId)
            throws NotFoundException, UnauthorizedActionException {

        reviewService.deleteReview(userId, reviewId);
        return ResponseEntity.ok(Map.of("deleted", true));
    } // end of deleteReview()

} // end of class
