import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { getTopRatedMovies, searchMovies, importMovie } from "../api.js";
import { useAuth } from "../context/AuthContext.jsx";

function Browse() {
    const [topRated, setTopRated] = useState([]);
    const [loadingTopRated, setLoadingTopRated] = useState(true);

    const [searchInput, setSearchInput] = useState("");
    const [searchResults, setSearchResults] = useState(null);
    const [searching, setSearching] = useState(false);
    const [error, setError] = useState(null);

    const { currentUser } = useAuth();
    const navigate = useNavigate();

    useEffect(() => {
        getTopRatedMovies()
            .then((data) => setTopRated(data))
            .catch((err) => setError(err.message))
            .finally(() => setLoadingTopRated(false));
    }, []);

    async function handleSearchSubmit(e) {
        e.preventDefault();

        if (!searchInput.trim()) {
            setSearchResults(null);
            return;
        }

        setSearching(true);
        setError(null);

        try {
            const results = await searchMovies(searchInput);
            setSearchResults(results);
        } catch (err) {
            setError(err.message);
        } finally {
            setSearching(false);
        }
    }

    function handleSearchInputChange(e) {
        const value = e.target.value;
        setSearchInput(value);

        if (!value.trim()) {
            setSearchResults(null);
        }
    }

    async function handleSearchResultClick(tmdbId) {
        if (!currentUser) {
            navigate("/login");
            return;
        }

        try {
            const movie = await importMovie(tmdbId);
            navigate(`/movies/${movie.id}`);
        } catch (err) {
            setError(err.message);
        }
    }

    const isSearchActive = searchResults !== null;

    return (
        <div>
            <div className="ledger-line">
                {isSearchActive
                    ? `— search results for "${searchInput}" —`
                    : `— now showing ${topRated.length} top-rated titles —`}
            </div>

            <form className="search-row" onSubmit={handleSearchSubmit}>
                <input
                    type="text"
                    placeholder="search the catalog..."
                    value={searchInput}
                    onChange={handleSearchInputChange}
                />
                <button type="submit" disabled={searching}>
                    {searching ? "searching..." : "find it"}
                </button>
            </form>

            {error && <p className="form-error">{error}</p>}

            {isSearchActive ? (
                <div className="grid">
                    {searchResults.length === 0 && <p>no results found.</p>}
                    {searchResults.map((result) => (
                        <div
                            key={result.id}
                            className="card"
                            onClick={() => handleSearchResultClick(result.id)}
                        >
                            {result.poster_path ? (
                                <img
                                    className="poster-img"
                                    src={`https://image.tmdb.org/t/p/w342${result.poster_path}`}
                                    alt={result.title}
                                />
                            ) : (
                                <div className="poster">{result.title}</div>
                            )}
                            <div className="card-body">
                                <p className="title">{result.title}</p>
                                <p className="meta">
                                    {result.release_date ? result.release_date.slice(0, 4) : "unknown year"}
                                </p>
                            </div>
                        </div>
                    ))}
                </div>
            ) : loadingTopRated ? (
                <p>loading...</p>
            ) : (
                <div className="grid">
                    {topRated.length === 0 && (
                        <p>no rated movies yet — search for one and be the first to review it.</p>
                    )}
                    {topRated.map(({ movie, averageRating }) => (
                        <div
                            key={movie.id}
                            className="card"
                            onClick={() => navigate(`/movies/${movie.id}`)}
                        >
                            <div className="stamp">
                                <b>{averageRating.toFixed(1)}</b>/10
                            </div>
                            {movie.posterPath ? (
                                <img
                                    className="poster-img"
                                    src={`https://image.tmdb.org/t/p/w342${movie.posterPath}`}
                                    alt={movie.title}
                                />
                            ) : (
                                <div className="poster">{movie.title}, {movie.releaseYear}</div>
                            )}
                            <div className="card-body">
                                <p className="title">{movie.title}</p>
                                <p className="meta">
                                    {movie.releaseYear} · {movie.runtimeMinutes ? `${movie.runtimeMinutes} min` : "? min"}
                                </p>
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
}

export default Browse;