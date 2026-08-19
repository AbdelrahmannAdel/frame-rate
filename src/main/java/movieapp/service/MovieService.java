package movieapp.service;

import movieapp.exception.DuplicateMovieException;
import movieapp.exception.NotFoundException;
import movieapp.model.Movie;
import movieapp.model.RatedMovie;
import movieapp.model.Review;
import movieapp.model.WatchlistEntry;
import movieapp.repository.MovieRepository;
import movieapp.repository.ReviewRepository;
import movieapp.repository.WatchlistEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class MovieService {

    private final MovieRepository movieRepository;
    private final ReviewRepository reviewRepository;
    private final WatchlistEntryRepository watchlistEntryRepository;

    public MovieService(MovieRepository movieRepository,
                        ReviewRepository reviewRepository,
                        WatchlistEntryRepository watchlistEntryRepository) {
        this.movieRepository = movieRepository;
        this.reviewRepository = reviewRepository;
        this.watchlistEntryRepository = watchlistEntryRepository;
    }

    public Movie createMovie(int tmdbId, String title, Integer releaseYear, String posterPath,
                             String overview, Integer runtimeMinutes) throws DuplicateMovieException {

        if (movieRepository.findByTmdbId(tmdbId).isPresent())
            throw new DuplicateMovieException("Movie already exists with tmdb_id: " + tmdbId);

        Movie movie = new Movie(tmdbId, title, releaseYear, posterPath, overview, runtimeMinutes);
        return movieRepository.save(movie);
    } // end of createMovie()

    @Transactional
    public boolean deleteMovie(int id) throws NotFoundException {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No movie found with id: " + id));

        List<Review> reviews = reviewRepository.findByMovie_Id(id);
        reviewRepository.deleteAll(reviews);

        List<WatchlistEntry> watchlist = watchlistEntryRepository.findByMovie_Id(id);
        watchlistEntryRepository.deleteAll(watchlist);

        movieRepository.delete(movie);
        return true;
    } // end of deleteMovie()

    public Movie getMovieById(int id) throws NotFoundException {
        return movieRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No movie found with id: " + id));
    } // end of getMovieById()

    public List<Movie> getAllMovies() {
        return movieRepository.findAll();
    } // end of getAllMovies()

    public List<RatedMovie> getTopRatedMovies() {
        List<Movie> allMovies = movieRepository.findAll();
        List<RatedMovie> ratedMovies = new ArrayList<>();

        for (Movie movie : allMovies) {
            List<Review> reviews = reviewRepository.findByMovie_Id(movie.getId());

            if (reviews.isEmpty())
                continue;

            double sum = 0;
            for (Review review : reviews)
                sum += review.getRating();

            double average = sum / reviews.size();
            ratedMovies.add(new RatedMovie(movie, average));
        }

        ratedMovies.sort((a, b) -> Double.compare(b.getAverageRating(), a.getAverageRating()));
        return ratedMovies;
    } // end of getTopRatedMovies()

    // searchAndImport(title) and importByTmdbId(tmdbId) INTENTIONALLY OMITTED --
    // both depend on movieapp.api.TmdbClient / TmdbMovieMapper / the TMDB DTOs,
    // none of which exist on this branch yet. Add once the api package is ported.

} // end of class