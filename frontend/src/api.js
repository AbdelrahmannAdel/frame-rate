const API_BASE = "http://localhost:8080";

export function saveToken(token) {
    localStorage.setItem("token", token);
}

export function getToken() {
    return localStorage.getItem("token");
}

export function clearToken() {
    localStorage.removeItem("token");
}

export function isLoggedIn() {
    return !!getToken();
}

// JWTs are base64-encoded, not encrypted -- the payload (the middle of the
// three dot-separated parts) is plain, readable JSON once decoded. We're not
// verifying the signature here (only the server can do that, since only it
// has the secret) -- this is purely a client-side convenience to read the
// userId claim back out of a token we already trust because we stored it
// ourselves right after a successful login/register
export function getUserIdFromToken() {
    const token = getToken();
    if (!token) return null;

    try {
        const payload = token.split(".")[1];
        const decoded = JSON.parse(atob(payload));
        return decoded.userId;
    } catch {
        return null;
    }
}

async function request(path, { method = "GET", body, auth = false, optionalAuth = false } = {}) {
    const headers = { "Content-Type": "application/json" };

    if (auth) {
        headers["Authorization"] = "Bearer " + getToken();
    } else if (optionalAuth) {
        const token = getToken();
        if (token) {
            headers["Authorization"] = "Bearer " + token;
        }
    }

    const response = await fetch(API_BASE + path, {
        method,
        headers,
        body: body ? JSON.stringify(body) : undefined,
    });

    if (!response.ok) {
        const errorText = await response.text();

        if (response.status === 401) {
            clearToken();
            window.dispatchEvent(new Event("auth:expired"));
        }

        throw new Error(errorText || `Request failed: ${response.status}`);
    }

    if (response.status === 204) return null;
    return response.json();
}

// ---- auth ----
export function register(username, email, password) {
    return request("/users", { method: "POST", body: { username, email, password } });
}

export function login(email, password) {
    return request("/login", { method: "POST", body: { email, password } });
}

// ---- movies ----
export function getTopRatedMovies() {
    return request("/movies/top-rated");
}

export function searchMovies(title) {
    return request(`/movies/search?title=${encodeURIComponent(title)}`);
}

export function importMovie(tmdbId) {
    return request("/movies/import", { method: "POST", body: { tmdbId }, auth: true });
}

export function getMovie(id) {
    return request(`/movies/${id}`);
}

export function getMovieReviews(movieId) {
    return request(`/movies/${movieId}/reviews`);
}

export function createReview(movieId, rating) {
    return request(`/movies/${movieId}/reviews`, { method: "POST", body: { rating }, auth: true });
}

export function updateReview(movieId, reviewId, rating) {
    return request(`/movies/${movieId}/reviews/${reviewId}`, { method: "PUT", body: { rating }, auth: true });
}

export function deleteReview(movieId, reviewId) {
    return request(`/movies/${movieId}/reviews/${reviewId}`, { method: "DELETE", auth: true });
}

// ---- watchlist ----
export function addToWatchlist(movieId) {
    return request("/users/watchlist", { method: "POST", body: { movieId }, auth: true });
}

export function removeFromWatchlist(entryId) {
    return request(`/users/watchlist/${entryId}`, { method: "DELETE", auth: true });
}

// ---- users / profile ----
export function getUser(id) {
    return request(`/users/${id}`, { optionalAuth: true });
}

export function searchUsers(username) {
    return request(`/users/search?username=${encodeURIComponent(username)}`);
}

export function getUserReviews(id) {
    return request(`/users/${id}/reviews`);
}

export function getUserWatchlist(id) {
    return request(`/users/${id}/watchlist`);
}

export function getFollowing(id) {
    return request(`/users/${id}/following`);
}

export function getFollowers(id) {
    return request(`/users/${id}/followers`);
}

export function followUser(followeeId) {
    return request("/users/following", { method: "POST", body: { followeeId }, auth: true });
}

export function unfollowUser(followeeId) {
    return request(`/users/following/${followeeId}`, { method: "DELETE", auth: true });
}

export function updateUsername(username) {
    return request("/users/username", { method: "PUT", body: { username }, auth: true });
}

export function updateEmail(email) {
    return request("/users/email", { method: "PUT", body: { email }, auth: true });
}

export function updatePassword(password) {
    return request("/users/password", { method: "PUT", body: { password }, auth: true });
}

export function deleteAccount() {
    return request("/users", { method: "DELETE", auth: true });
}

// ---- compatibility ----
export function getCompatibility(otherUserId) {
    return request(`/users/compatibility/${otherUserId}`, { auth: true });
}