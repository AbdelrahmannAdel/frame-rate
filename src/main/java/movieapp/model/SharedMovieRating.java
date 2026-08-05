package movieapp.model;

// one row of the you-vs-them comparison: a single movie both users have reviewed,
// paired with each user's own rating for it
public class SharedMovieRating {

    private final int movieId;
    private final String title;
    private final int myRating;
    private final int theirRating;

    public SharedMovieRating(int movieId, String title, int myRating, int theirRating) {
        this.movieId = movieId;
        this.title = title;
        this.myRating = myRating;
        this.theirRating = theirRating;
    }

    public int getMovieId() {
        return movieId;
    }

    public String getTitle() {
        return title;
    }

    public int getMyRating() {
        return myRating;
    }

    public int getTheirRating() {
        return theirRating;
    }

} // end of class