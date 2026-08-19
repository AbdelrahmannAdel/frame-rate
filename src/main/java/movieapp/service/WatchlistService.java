package movieapp.service;

import movieapp.exception.*;
import movieapp.model.Movie;
import movieapp.model.User;
import movieapp.model.WatchlistEntry;
import movieapp.repository.MovieRepository;
import movieapp.repository.UserRepository;
import movieapp.repository.WatchlistEntryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WatchlistService {

    private final WatchlistEntryRepository watchlistEntryRepository;
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;

    public WatchlistService(WatchlistEntryRepository watchlistEntryRepository,
                            UserRepository userRepository,
                            MovieRepository movieRepository) {
        this.watchlistEntryRepository = watchlistEntryRepository;
        this.userRepository = userRepository;
        this.movieRepository = movieRepository;
    }

    public WatchlistEntry addToWatchlist(int userId, int movieId)
            throws NotFoundException, DuplicateWatchlistException {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("No user found with id: " + userId));
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new NotFoundException("No movie found with id: " + movieId));

        for (WatchlistEntry entry : watchlistEntryRepository.findByUser_Id(userId)) {
            if (entry.getMovie().getId().equals(movieId))
                throw new DuplicateWatchlistException("User " + userId + " already has movie " + movieId + " in their watchlist");
        }

        return watchlistEntryRepository.save(new WatchlistEntry(user, movie));
    } // end of addToWatchlist()

    public boolean removeFromWatchlist(int userId, int id) throws NotFoundException, UnauthorizedActionException {
        WatchlistEntry entry = watchlistEntryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No watchlist entry found with id: " + id));

        if (!entry.getUser().getId().equals(userId))
            throw new UnauthorizedActionException("User " + userId + " is not authorized to remove watchlist entry " + id);

        watchlistEntryRepository.delete(entry);
        return true;
    } // end of removeFromWatchlist()

    public List<WatchlistEntry> getWatchlistByUser(int userId) throws NotFoundException {
        if (userRepository.findById(userId).isEmpty())
            throw new NotFoundException("No user found with id: " + userId);

        return watchlistEntryRepository.findByUser_Id(userId);
    } // end of getWatchlistByUser()

} // end of class