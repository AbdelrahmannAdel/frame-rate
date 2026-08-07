import { useEffect, useState } from "react";
import { Link, useParams, useNavigate } from "react-router-dom";
import {
    getMovie,
    getMovieReviews,
    createReview,
    updateReview,
    addToWatchlist,
    getUserWatchlist,
} from "../api.js";
import { useAuth } from "../context/AuthContext.jsx";
import StarRating from "../components/StarRating.jsx";

function MovieDetail() {
    const { id } = useParams();
    const { currentUser } = useAuth();
    const navigate = useNavigate();

    const [movie, setMovie] = useState(null);
    const [reviews, setReviews] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const [ratingInput, setRatingInput] = useState(5);
    const [submittingRating, setSubmittingRating] = useState(false);

    const [inWatchlist, setInWatchlist] = useState(false);
    const [addingToWatchlist, setAddingToWatchlist] = useState(false);

    useEffect(() => {
        setLoading(true);
        Promise.all([getMovie(id), getMovieReviews(id)])
            .then(([movieData, reviewsData]) => {
                setMovie(movieData);
                setReviews(reviewsData);
            })
            .catch((err) => setError(err.message))
            .finally(() => setLoading(false));
    }, [id]);

    useEffect(() => {
        if (!currentUser) return;

        getUserWatchlist(currentUser.id)
            .then((entries) => {
                const alreadyOn = entries.some((entry) => entry.movieId === Number(id));
                setInWatchlist(alreadyOn);
            })
            .catch(() => {});
    }, [currentUser, id]);

    const myReview = currentUser
        ? reviews.find((r) => r.userId === currentUser.id)
        : null;

    async function handleRatingSubmit() {
        if (!currentUser) {
            navigate("/login");
            return;
        }

        setSubmittingRating(true);
        setError(null);

        try {
            if (myReview) {
                const updated = await updateReview(id, myReview.id, ratingInput);
                setReviews((prev) => prev.map((r) => (r.id === updated.id ? updated : r)));
            } else {
                const created = await createReview(id, ratingInput);
                setReviews((prev) => [...prev, created]);
            }
        } catch (err) {
            setError(err.message);
        } finally {
            setSubmittingRating(false);
        }
    }

    async function handleAddToWatchlist() {
        if (!currentUser) {
            navigate("/login");
            return;
        }

        setAddingToWatchlist(true);
        setError(null);

        try {
            await addToWatchlist(Number(id));
            setInWatchlist(true);
        } catch (err) {
            setError(err.message);
        } finally {
            setAddingToWatchlist(false);
        }
    }

    if (loading) return <p>loading...</p>;
    if (!movie) return <p>movie not found.</p>;

    const averageRating =
        reviews.length > 0
            ? (reviews.reduce((sum, r) => sum + r.rating, 0) / reviews.length).toFixed(1)
            : null;

    return (
        <div>
            <div className="movie-detail">
                {movie.posterPath ? (
                    <img
                        className="detail-poster"
                        src={`https://image.tmdb.org/t/p/w342${movie.posterPath}`}
                        alt={movie.title}
                    />
                ) : (
                    <div className="detail-poster detail-poster-placeholder">{movie.title}</div>
                )}

                <div className="detail-info">
                    <h1>{movie.title}</h1>
                    <p className="meta">
                        {movie.releaseYear} · {movie.runtimeMinutes ? `${movie.runtimeMinutes} min` : "unknown runtime"}
                    </p>
                    {averageRating && (
                        <p className="meta">average rating: {averageRating}/10 ({reviews.length} review{reviews.length !== 1 ? "s" : ""})</p>
                    )}
                    <p>{movie.overview}</p>

                    <button onClick={handleAddToWatchlist} disabled={inWatchlist || addingToWatchlist}>
                        {inWatchlist ? "in watchlist" : addingToWatchlist ? "adding..." : "+ watchlist"}
                    </button>
                </div>
            </div>

            {error && <p className="form-error">{error}</p>}

            <div className="rate-box">
                <h2>{myReview ? "update your rating" : "rate this movie"}</h2>
                <StarRating value={ratingInput} onChange={setRatingInput} />
                <button onClick={handleRatingSubmit} disabled={submittingRating}>
                    {submittingRating ? "saving..." : myReview ? "update" : "submit"}
                </button>
            </div>

            <div className="reviews-box">
                <h2>reviews ({reviews.length})</h2>
                {reviews.length === 0 && <p>no reviews yet — be the first.</p>}
                <ul className="reviews-list">
                    {reviews.map((review) => (
                        <li key={review.id}>
                            <Link to={`/profile/${review.userId}`}>user #{review.userId}</Link>
                            {" — "}
                            <StarRating value={review.rating} size="small" />
                        </li>
                    ))}
                </ul>
            </div>
        </div>
    );
}

export default MovieDetail;