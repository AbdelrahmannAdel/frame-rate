package movieapp.repository;

import movieapp.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Integer> {

    List<Review> findByUser_Id(int userId);

    List<Review> findByMovie_Id(int movieId);

} // end of interface