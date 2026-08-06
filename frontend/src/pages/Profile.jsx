import { useEffect, useState } from "react";
import { useParams, useNavigate, Link } from "react-router-dom";
import {
    getUser,
    getUserReviews,
    getUserWatchlist,
    getFollowing,
    getFollowers,
    followUser,
    unfollowUser,
    removeFromWatchlist,
    deleteReview,
    updateUsername,
    updateEmail,
    updatePassword,
    getMovie,
} from "../api.js";
import { useAuth } from "../context/AuthContext.jsx";
import StarRating from "../components/StarRating.jsx";

function Profile() {
    const { id } = useParams();
    const { currentUser, logout } = useAuth();
    const navigate = useNavigate();

    const [profileUser, setProfileUser] = useState(null);
    const [reviews, setReviews] = useState([]);
    const [watchlist, setWatchlist] = useState([]);
    const [following, setFollowing] = useState([]);
    const [followers, setFollowers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const [isFollowingThem, setIsFollowingThem] = useState(false);
    const [theyFollowMe, setTheyFollowMe] = useState(false);
    const [followBusy, setFollowBusy] = useState(false);

    const isOwnProfile = currentUser && Number(id) === currentUser.id;

    useEffect(() => {
        setLoading(true);
        setError(null);

        Promise.all([
            getUser(id),
            getUserReviews(id),
            getUserWatchlist(id),
            getFollowing(id),
            getFollowers(id),
        ])
            .then(async ([userData, reviewsData, watchlistData, followingData, followersData]) => {
                setProfileUser(userData);
                setFollowing(followingData);
                setFollowers(followersData);

                const reviewMovies = await Promise.all(
                    reviewsData.map((r) => getMovie(r.movieId).then((m) => ({ ...r, movie: m })))
                );
                const watchlistMovies = await Promise.all(
                    watchlistData.map((w) => getMovie(w.movieId).then((m) => ({ ...w, movie: m })))
                );

                setReviews(reviewMovies);
                setWatchlist(watchlistMovies);
            })
            .catch((err) => setError(err.message))
            .finally(() => setLoading(false));
    }, [id]);

    useEffect(() => {
        if (!currentUser || isOwnProfile) return;

        Promise.all([getFollowing(currentUser.id), getFollowers(currentUser.id)])
            .then(([myFollowing, myFollowers]) => {
                setIsFollowingThem(myFollowing.some((u) => u.id === Number(id)));
                setTheyFollowMe(myFollowers.some((u) => u.id === Number(id)));
            })
            .catch(() => {});
    }, [currentUser, isOwnProfile, id]);

    async function handleRemoveFromWatchlist(entryId) {
        setError(null);
        try {
            await removeFromWatchlist(entryId);
            setWatchlist((prev) => prev.filter((w) => w.id !== entryId));
        } catch (err) {
            setError(err.message);
        }
    }

    async function handleRemoveReview(movieId, reviewId) {
        setError(null);
        try {
            await deleteReview(movieId, reviewId);
            setReviews((prev) => prev.filter((r) => r.id !== reviewId));
        } catch (err) {
            setError(err.message);
        }
    }

    async function handleFollowToggle() {
        if (!currentUser) {
            navigate("/login");
            return;
        }

        setFollowBusy(true);
        setError(null);

        try {
            if (isFollowingThem) {
                await unfollowUser(Number(id));
                setIsFollowingThem(false);
            } else {
                await followUser(Number(id));
                setIsFollowingThem(true);
            }
        } catch (err) {
            setError(err.message);
        } finally {
            setFollowBusy(false);
        }
    }

    if (loading) return <p>loading...</p>;
    if (!profileUser) return <p>user not found.</p>;

    const isMutual = isFollowingThem && theyFollowMe;

    return (
        <div>
            <div className="profile-header">
                <h1>{profileUser.username}</h1>
                <p className="meta">
                    {followers.length} follower{followers.length !== 1 ? "s" : ""} · {following.length} following
                </p>

                {!isOwnProfile && currentUser && (
                    <div className="profile-actions">
                        <button onClick={handleFollowToggle} disabled={followBusy}>
                            {isFollowingThem ? "unfollow" : "follow"}
                        </button>

                        <button
                            disabled={!isMutual}
                            onClick={() => navigate(`/compatibility/${id}`)}
                            title={isMutual ? "" : "you must mutually follow each other to check compatibility"}
                        >
                            check compatibility
                        </button>
                    </div>
                )}
            </div>

            {error && <p className="form-error">{error}</p>}

            <div className="profile-section">
                <h2>ratings ({reviews.length})</h2>
                {reviews.length === 0 && <p>no ratings yet.</p>}
                <ul className="profile-list">
                    {reviews.map((r) => (
                        <li key={r.id}>
                            <span>
                              <Link to={`/movies/${r.movie.id}`}>{r.movie.title}</Link>
                                {" — "}
                                <StarRating value={r.rating} size="small" />
                            </span>
                            {isOwnProfile && (
                                <button className="remove-button" onClick={() => handleRemoveReview(r.movie.id, r.id)}>
                                    remove
                                </button>
                            )}
                        </li>
                    ))}
                </ul>
            </div>

            <div className="profile-section">
                <h2>watchlist ({watchlist.length})</h2>
                {watchlist.length === 0 && <p>watchlist is empty.</p>}
                <ul className="profile-list">
                    {watchlist.map((w) => (
                        <li key={w.id}>
                            <Link to={`/movies/${w.movie.id}`}>{w.movie.title}</Link>
                            {isOwnProfile && (
                                <button className="remove-button" onClick={() => handleRemoveFromWatchlist(w.id)}>
                                    remove
                                </button>
                            )}
                        </li>
                    ))}
                </ul>
            </div>

            <div className="profile-section">
                <h2>following ({following.length})</h2>
                <ul className="profile-list">
                    {following.map((u) => (
                        <li key={u.id}>
                            <Link to={`/profile/${u.id}`}>{u.username}</Link>
                        </li>
                    ))}
                </ul>
            </div>

            <div className="profile-section">
                <h2>followers ({followers.length})</h2>
                <ul className="profile-list">
                    {followers.map((u) => (
                        <li key={u.id}>
                            <Link to={`/profile/${u.id}`}>{u.username}</Link>
                        </li>
                    ))}
                </ul>
            </div>

            {isOwnProfile && <SettingsSection user={profileUser} onUpdated={setProfileUser} logout={logout} />}
        </div>
    );
}

function SettingsSection({ user, onUpdated, logout }) {
    const [username, setUsername] = useState(user.username);
    const [email, setEmail] = useState(user.email);
    const [password, setPassword] = useState("");
    const [error, setError] = useState(null);
    const [message, setMessage] = useState(null);
    const [busy, setBusy] = useState(false);

    async function handleUsernameSubmit(e) {
        e.preventDefault();
        setBusy(true);
        setError(null);
        setMessage(null);
        try {
            const updated = await updateUsername(username);
            onUpdated(updated);
            setMessage("username updated.");
        } catch (err) {
            setError(err.message);
        } finally {
            setBusy(false);
        }
    }

    async function handleEmailSubmit(e) {
        e.preventDefault();
        setBusy(true);
        setError(null);
        setMessage(null);
        try {
            const updated = await updateEmail(email);
            onUpdated(updated);
            setMessage("email updated.");
        } catch (err) {
            setError(err.message);
        } finally {
            setBusy(false);
        }
    }

    async function handlePasswordSubmit(e) {
        e.preventDefault();
        setBusy(true);
        setError(null);
        setMessage(null);
        try {
            await updatePassword(password);
            setPassword("");
            setMessage("password updated.");
        } catch (err) {
            setError(err.message);
        } finally {
            setBusy(false);
        }
    }

    return (
        <div className="settings-box">
            <h2>settings</h2>

            {error && <p className="form-error">{error}</p>}
            {message && <p className="form-message">{message}</p>}

            <form onSubmit={handleUsernameSubmit} className="settings-form">
                <input value={username} onChange={(e) => setUsername(e.target.value)} />
                <button type="submit" disabled={busy}>update username</button>
            </form>

            <form onSubmit={handleEmailSubmit} className="settings-form">
                <input value={email} onChange={(e) => setEmail(e.target.value)} />
                <button type="submit" disabled={busy}>update email</button>
            </form>

            <form onSubmit={handlePasswordSubmit} className="settings-form">
                <input
                    type="password"
                    placeholder="new password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                />
                <button type="submit" disabled={busy}>update password</button>
            </form>

            <button onClick={logout} className="logout-button">log out</button>
        </div>
    );
}

export default Profile;