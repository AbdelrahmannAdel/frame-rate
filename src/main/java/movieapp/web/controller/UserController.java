package movieapp.web.controller;

import movieapp.auth.JwtService;
import movieapp.auth.PasswordHasher;
import movieapp.exception.*;
import movieapp.model.*;
import movieapp.repository.UserRepository;
import movieapp.service.*;
import movieapp.web.dto.request.*;
import movieapp.web.dto.response.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final ReviewService reviewService;
    private final WatchlistService watchlistService;
    private final FollowService followService;
    private final CompatibilityService compatibilityService;

    public UserController(UserService userService,
                          UserRepository userRepository,
                          JwtService jwtService,
                          ReviewService reviewService,
                          WatchlistService watchlistService,
                          FollowService followService,
                          CompatibilityService compatibilityService) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.reviewService = reviewService;
        this.watchlistService = watchlistService;
        this.followService = followService;
        this.compatibilityService = compatibilityService;
    }

    // ================ GET ================

    @GetMapping("/users/search")
    public ResponseEntity<List<UserResponse>> searchUsers(@RequestParam String username) {
        List<User> users = userService.searchUsers(username);
        List<UserResponse> responseList = users.stream()
                .map(u -> new UserResponse(u, false))
                .toList();
        return ResponseEntity.ok(responseList);
    } // end of searchUsers()

    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponse> getUser(
            @PathVariable int id,
            @AuthenticationPrincipal Integer callerId) throws NotFoundException {

        boolean includeEmail = callerId != null && callerId.equals(id);
        User user = userService.getUserById(id);
        return ResponseEntity.ok(new UserResponse(user, includeEmail));
    } // end of getUser()

    @GetMapping("/users/{id}/reviews")
    public ResponseEntity<List<ReviewResponse>> getUserReviews(@PathVariable int id) throws NotFoundException {
        List<ReviewResponse> responseList = reviewService.getReviewsByUser(id).stream()
                .map(review -> {
                    User reviewer = userRepository.findById(review.getUser().getId())
                            .orElseThrow(() -> new IllegalStateException(
                                    "Review " + review.getId() + " refers to a missing user"));
                    return new ReviewResponse(review, reviewer.getUsername());
                })
                .toList();

        return ResponseEntity.ok(responseList);
    } // end of getUserReviews()

    @GetMapping("/users/{id}/watchlist")
    public ResponseEntity<List<WatchlistEntryResponse>> getUserWatchlist(@PathVariable int id) throws NotFoundException {
        List<WatchlistEntry> entries = watchlistService.getWatchlistByUser(id);
        List<WatchlistEntryResponse> responseList = entries.stream()
                .map(WatchlistEntryResponse::new)
                .toList();
        return ResponseEntity.ok(responseList);
    } // end of getUserWatchlist()

    @GetMapping("/users/{id}/following")
    public ResponseEntity<List<UserResponse>> getFollowing(@PathVariable int id) throws NotFoundException {
        List<User> following = followService.getFollowing(id);
        List<UserResponse> responseList = following.stream()
                .map(u -> new UserResponse(u, false))
                .toList();
        return ResponseEntity.ok(responseList);
    } // end of getFollowing()

    @GetMapping("/users/{id}/followers")
    public ResponseEntity<List<UserResponse>> getFollowers(@PathVariable int id) throws NotFoundException {
        List<User> followers = followService.getFollowers(id);
        List<UserResponse> responseList = followers.stream()
                .map(u -> new UserResponse(u, false))
                .toList();
        return ResponseEntity.ok(responseList);
    } // end of getFollowers()

    @GetMapping("/users/compatibility/{otherId}")
    public ResponseEntity<CompatibilityResult> getCompatibility(
            @AuthenticationPrincipal int userId,
            @PathVariable int otherId) throws NotFoundException, NotMutualFollowException {

        CompatibilityResult result = compatibilityService.getCompatibility(userId, otherId);
        return ResponseEntity.ok(result);
    } // end of getCompatibility()

    // ================ PUT ================

    @PutMapping("/users/username")
    public ResponseEntity<UserResponse> updateUsername(
            @AuthenticationPrincipal Integer userId,
            @RequestBody UpdateUsernameRequest request) throws NotFoundException, DuplicateUsernameException {

        User updated = userService.updateUsername(userId, request.username());
        return ResponseEntity.ok(new UserResponse(updated, true));
    } // end of updateUsername()

    @PutMapping("/users/email")
    public ResponseEntity<UserResponse> updateEmail(
            @AuthenticationPrincipal Integer userId,
            @RequestBody UpdateEmailRequest request) throws NotFoundException, DuplicateEmailException {

        User updated = userService.updateEmail(userId, request.email());
        return ResponseEntity.ok(new UserResponse(updated, true));
    } // end of updateEmail()

    @PutMapping("/users/password")
    public ResponseEntity<UserResponse> updatePassword(
            @AuthenticationPrincipal Integer userId,
            @RequestBody UpdatePasswordRequest request) throws NotFoundException {

        String hashedPassword = PasswordHasher.hash(request.password());
        User updated = userService.updatePassword(userId, hashedPassword);
        return ResponseEntity.ok(new UserResponse(updated, true));
    } // end of updatePassword()

    // ================ POST ================

    @PostMapping("/users")
    public ResponseEntity<Map<String, Object>> register(@RequestBody RegisterUserRequest request)
            throws DuplicateUsernameException, DuplicateEmailException {

        String hashedPassword = PasswordHasher.hash(request.password());

        User user = userService.registerUser(request.username(), request.email(), hashedPassword);
        UserResponse userResponse = new UserResponse(user, true);
        String token = jwtService.generateToken(user.getId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("user", userResponse, "token", token));
    } // end of register()

    @PostMapping("/users/watchlist")
    public ResponseEntity<WatchlistEntryResponse> addToWatchlist(
            @AuthenticationPrincipal Integer userId,
            @RequestBody AddToWatchlistRequest request) throws NotFoundException, DuplicateWatchlistException {

        WatchlistEntry entry = watchlistService.addToWatchlist(userId, request.movieId());
        return ResponseEntity.status(HttpStatus.CREATED).body(new WatchlistEntryResponse(entry));
    } // end of addToWatchlist()

    @PostMapping("/users/following")
    public ResponseEntity<FollowResponse> followUser(
            @AuthenticationPrincipal Integer userId,
            @RequestBody FollowUserRequest request)
            throws NotFoundException, SelfFollowException, DuplicateFollowException {

        Follow follow = followService.followUser(userId, request.followeeId());
        return ResponseEntity.status(HttpStatus.CREATED).body(new FollowResponse(follow));
    } // end of followUser()

    // ================ DELETE ================

    @DeleteMapping("/users")
    public ResponseEntity<Map<String, Boolean>> deleteAccount(
            @AuthenticationPrincipal Integer userId) throws NotFoundException {

        userService.deleteUser(userId);
        return ResponseEntity.ok(Map.of("deleted", true));
    } // end of deleteAccount()

    @DeleteMapping("/users/watchlist/{entryId}")
    public ResponseEntity<Map<String, Boolean>> removeFromWatchlist(
            @AuthenticationPrincipal Integer userId,
            @PathVariable int entryId) throws NotFoundException, UnauthorizedActionException {

        watchlistService.removeFromWatchlist(userId, entryId);
        return ResponseEntity.ok(Map.of("deleted", true));
    } // end of removeFromWatchlist()

    @DeleteMapping("/users/following/{followeeId}")
    public ResponseEntity<Map<String, Boolean>> unfollowUser(
            @AuthenticationPrincipal Integer userId,
            @PathVariable int followeeId) throws NotFoundException {

        followService.unfollowUser(userId, followeeId);
        return ResponseEntity.ok(Map.of("deleted", true));
    } // end of unfollowUser()

} // end of class