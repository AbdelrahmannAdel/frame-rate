package movieapp.service;

import movieapp.db.UserRepository;
import movieapp.db.WatchlistRepository;
import movieapp.exception.DuplicateWatchlistException;
import movieapp.exception.NotFoundException;
import movieapp.exception.UnauthorizedActionException;
import movieapp.model.WatchlistEntry;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class WatchlistService {

    private final Connection connection;

    public WatchlistService(Connection connection){
        this.connection = connection;
    }

    public WatchlistEntry addToWatchlist(int userId, int movieId) throws SQLException, DuplicateWatchlistException {
        WatchlistRepository watchlistRepository = new WatchlistRepository();

        List<WatchlistEntry> watchList = watchlistRepository.findByUser(connection, userId);
        for (WatchlistEntry watchlistEntry: watchList){
            if (watchlistEntry.getMovieId() == movieId)
                throw new DuplicateWatchlistException("User " + userId + " already has movie " + movieId + " in their watchlist");
        }

        return watchlistRepository.add(connection, userId, movieId);
    } // end of addToWatchList()

    public boolean removeFromWatchlist(int userId, int id) throws SQLException, NotFoundException, UnauthorizedActionException {
        WatchlistRepository watchlistRepository = new WatchlistRepository();

        WatchlistEntry watchlistEntry = watchlistRepository.findById(connection, id);

        if (watchlistEntry == null)
            throw new NotFoundException("No watchlist entry found with id: " + id);

        if (watchlistEntry.getUserId() != userId)
            throw new UnauthorizedActionException("User " + userId + " is not authorized to remove watchlist entry " + id);

        return watchlistRepository.remove(connection, id);
    } // end of removeFromWatchlist()

    public List<WatchlistEntry> getWatchlistByUser(int userId) throws SQLException, NotFoundException {
        UserRepository userRepository = new UserRepository();
        if (userRepository.findById(connection, userId) == null)
            throw new NotFoundException("No user found with id: " + userId);

        WatchlistRepository watchlistRepository = new WatchlistRepository();
        return watchlistRepository.findByUser(connection, userId);
    } // end of getWatchlistByUser()

} // end of class