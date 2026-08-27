package movieapp.repository;

import movieapp.model.WatchlistEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WatchlistEntryRepository extends JpaRepository<WatchlistEntry, Integer> {

    List<WatchlistEntry> findByUser_Id(int userId);

    List<WatchlistEntry> findByMovie_Id(int movieId);

} // end of interface