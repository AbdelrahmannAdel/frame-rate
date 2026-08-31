import { useEffect, useState, useRef } from "react";
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
    uploadAvatar,
    getMovie,
} from "../api.js";
import { useAuth } from "../context/AuthContext.jsx";

const API_BASE = "http://localhost:8080";

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
        // eslint-disable-next-line react-hooks/set-state-in-effect -- initial
        // loading/error reset before an async fetch; not the accidental-loop
        // pattern this rule is meant to catch
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
                <div className="profile-header-top">
                    {profileUser.avatarUrl ? (
                        <img
                            className="avatar-img"
                            src={`${API_BASE}${profileUser.avatarUrl}`}
                            alt={`${profileUser.username}'s avatar`}
                        />
                    ) : (
                        <div className="avatar-placeholder">
                            {profileUser.username.charAt(0).toUpperCase()}
                        </div>
                    )}

                    <div>
                        <h1>{profileUser.username}</h1>
                        <p className="meta">
                            <Link to={`/profile/${id}/followers`}>
                                {followers.length} follower{followers.length !== 1 ? "s" : ""}
                            </Link>
                            {" · "}
                            <Link to={`/profile/${id}/following`}>
                                {following.length} following
                            </Link>
                        </p>
                    </div>
                </div>

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
                <div className="grid">
                    {reviews.map((r) => (
                        <div key={r.id} className="card" onClick={() => navigate(`/movies/${r.movie.id}`)}>
                            <div className="stamp">
                                <b>{r.rating}</b>/10
                            </div>
                            {r.movie.posterPath ? (
                                <img
                                    className="poster-img"
                                    src={`https://image.tmdb.org/t/p/w342${r.movie.posterPath}`}
                                    alt={r.movie.title}
                                />
                            ) : (
                                <div className="poster">{r.movie.title}</div>
                            )}
                            <div className="card-body">
                                <p className="title">{r.movie.title}</p>
                                <p className="meta">{r.movie.releaseYear}</p>
                            </div>
                            {isOwnProfile && (
                                <button
                                    className="remove-button card-remove"
                                    onClick={(e) => {
                                        e.stopPropagation();
                                        handleRemoveReview(r.movie.id, r.id);
                                    }}
                                >
                                    remove
                                </button>
                            )}
                        </div>
                    ))}
                </div>
            </div>

            <div className="profile-section">
                <h2>watchlist ({watchlist.length})</h2>
                {watchlist.length === 0 && <p>watchlist is empty.</p>}
                <div className="grid">
                    {watchlist.map((w) => (
                        <div key={w.id} className="card" onClick={() => navigate(`/movies/${w.movie.id}`)}>
                            {w.movie.posterPath ? (
                                <img
                                    className="poster-img"
                                    src={`https://image.tmdb.org/t/p/w342${w.movie.posterPath}`}
                                    alt={w.movie.title}
                                />
                            ) : (
                                <div className="poster">{w.movie.title}</div>
                            )}
                            <div className="card-body">
                                <p className="title">{w.movie.title}</p>
                                <p className="meta">{w.movie.releaseYear}</p>
                            </div>
                            {isOwnProfile && (
                                <button
                                    className="remove-button card-remove"
                                    onClick={(e) => {
                                        e.stopPropagation();
                                        handleRemoveFromWatchlist(w.id);
                                    }}
                                >
                                    remove
                                </button>
                            )}
                        </div>
                    ))}
                </div>
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

    const [avatarPreview, setAvatarPreview] = useState(null);
    const [avatarUploading, setAvatarUploading] = useState(false);
    const avatarInputRef = useRef(null);

    // must match the backend's spring.servlet.multipart.max-file-size --
    // duplicated here deliberately so we can reject an oversized file
    // instantly, client-side, before ever attempting the upload (some
    // servers reset the connection outright on an oversized multipart
    // body rather than returning a clean error, which this sidesteps)
    const MAX_AVATAR_BYTES = 5 * 1024 * 1024;

    useEffect(() => {
        return () => {
            if (avatarPreview) URL.revokeObjectURL(avatarPreview);
        };
    }, [avatarPreview]);

    function handleAvatarClick() {
        avatarInputRef.current?.click();
    }

    async function handleAvatarFileChange(e) {
        const file = e.target.files[0];
        if (!file) return;

        setError(null);
        setMessage(null);

        if (file.size > MAX_AVATAR_BYTES) {
            setError(`That file is too large (${(file.size / (1024 * 1024)).toFixed(1)}MB) — the limit is 5MB.`);
            e.target.value = "";
            return;
        }

        const preview = URL.createObjectURL(file);
        setAvatarPreview(preview);
        setAvatarUploading(true);

        try {
            const updated = await uploadAvatar(file);
            onUpdated(updated);
            setMessage("avatar updated.");
        } catch (err) {
            setError(err.message);
        } finally {
            setAvatarUploading(false);
            URL.revokeObjectURL(preview);
            setAvatarPreview(null);
            e.target.value = "";
        }
    }

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

            <div className="avatar-settings">
                <div
                    className="avatar-settings-preview avatar-clickable"
                    onClick={handleAvatarClick}
                    title="click to change avatar"
                >
                    {avatarPreview ? (
                        <img className="avatar-img" src={avatarPreview} alt="new avatar preview" />
                    ) : user.avatarUrl ? (
                        <img className="avatar-img" src={`${API_BASE}${user.avatarUrl}`} alt="current avatar" />
                    ) : (
                        <div className="avatar-placeholder">{user.username.charAt(0).toUpperCase()}</div>
                    )}

                    {avatarUploading && <div className="avatar-uploading-overlay">uploading...</div>}

                    <input
                        ref={avatarInputRef}
                        type="file"
                        accept="image/jpeg,image/png"
                        onChange={handleAvatarFileChange}
                        style={{ display: "none" }}
                    />
                </div>
                <p className="meta">click your avatar to change it</p>
            </div>

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