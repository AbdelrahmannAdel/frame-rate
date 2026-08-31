package movieapp.service;

import movieapp.auth.PasswordHasher;
import movieapp.exception.*;
import movieapp.model.Follow;
import movieapp.model.Review;
import movieapp.model.User;
import movieapp.model.WatchlistEntry;
import movieapp.repository.FollowRepository;
import movieapp.repository.ReviewRepository;
import movieapp.repository.UserRepository;
import movieapp.repository.WatchlistEntryRepository;
import movieapp.storage.FileStorageService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final WatchlistEntryRepository watchlistEntryRepository;
    private final FollowRepository followRepository;
    private final FileStorageService fileStorageService;
    private final ImageValidator imageValidator;

    public UserService(UserRepository userRepository,
                       ReviewRepository reviewRepository,
                       WatchlistEntryRepository watchlistEntryRepository,
                       FollowRepository followRepository,
                       FileStorageService fileStorageService,
                       ImageValidator imageValidator) {
        this.userRepository = userRepository;
        this.reviewRepository = reviewRepository;
        this.watchlistEntryRepository = watchlistEntryRepository;
        this.followRepository = followRepository;
        this.fileStorageService = fileStorageService;
        this.imageValidator = imageValidator;
    }

    public User registerUser(String username, String email, String passwordHash)
            throws DuplicateUsernameException, DuplicateEmailException {

        if (userRepository.findByUsername(username).isPresent())
            throw new DuplicateUsernameException("Username already taken: " + username);

        if (userRepository.findByEmail(email).isPresent())
            throw new DuplicateEmailException("Email already taken: " + email);

        User user = new User(username, email, passwordHash);
        return userRepository.save(user);
    } // end of registerUser()

    @Transactional
    public void deleteUser(int userId) throws NotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("No user found with id: " + userId));

        List<Review> reviews = reviewRepository.findByUser_Id(userId);
        reviewRepository.deleteAll(reviews);

        List<WatchlistEntry> watchlist = watchlistEntryRepository.findByUser_Id(userId);
        watchlistEntryRepository.deleteAll(watchlist);

        List<Follow> following = followRepository.findByFollower_Id(userId);
        followRepository.deleteAll(following);

        List<Follow> followers = followRepository.findByFollowee_Id(userId);
        followRepository.deleteAll(followers);

        userRepository.delete(user);
    } // end of deleteUser()

    public User getUserById(int id) throws NotFoundException {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No user found with id: " + id));
    } // end of getUserById()

    public User updateUsername(int userId, String newUsername)
            throws NotFoundException, DuplicateUsernameException {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("No user found with id: " + userId));

        var existing = userRepository.findByUsername(newUsername);
        if (existing.isPresent() && !existing.get().getId().equals(userId))
            throw new DuplicateUsernameException("Username already taken: " + newUsername);

        user.setUsername(newUsername);
        return userRepository.save(user);
    } // end of updateUsername()

    public User updateEmail(int userId, String newEmail)
            throws NotFoundException, DuplicateEmailException {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("No user found with id: " + userId));

        var existing = userRepository.findByEmail(newEmail);
        if (existing.isPresent() && !existing.get().getId().equals(userId))
            throw new DuplicateEmailException("Email already taken: " + newEmail);

        user.setEmail(newEmail);
        return userRepository.save(user);
    } // end of updateEmail()

    public User updatePassword(int userId, String newPasswordHash) throws NotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("No user found with id: " + userId));

        user.setPasswordHash(newPasswordHash);
        return userRepository.save(user);
    } // end of updatePassword()

    public User login(String email, String password) throws InvalidCredentialsException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("Incorrect email"));

        if (!PasswordHasher.matches(password, user.getPasswordHash()))
            throw new InvalidCredentialsException("Incorrect password");

        return user;
    } // end of login()

    public List<User> searchUsers(String username) {
        return userRepository.findByUsernameContainingIgnoreCase(username);
    } // end of searchUsers()

    public User updateAvatar(int userId, MultipartFile file)
            throws NotFoundException, InvalidImageException, IOException {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("No user found with id: " + userId));

        String extension = imageValidator.detectExtension(file);
        String filename = UUID.randomUUID() + "." + extension;

        String newPath = fileStorageService.store(file, filename);

        if (user.getAvatarPath() != null)
            fileStorageService.delete(user.getAvatarPath());

        user.setAvatarPath(newPath);
        return userRepository.save(user);
    } // end of updateAvatar()

} // end of class