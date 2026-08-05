import { useEffect, useState } from "react";
import { getTopRatedMovies } from "../api.js";

function Browse() {
    const [movies, setMovies] = useState([]);

    useEffect(() => {
        getTopRatedMovies()
            .then((data) => setMovies(data))
            .catch((err) => console.error("Failed to load movies:", err));
    }, []);

    return (
        <div>
            <h1>Browse page</h1>
            <ul>
                {movies.map((entry) => (
                    <li key={entry.movie.id}>
                        {entry.movie.title} ({entry.movie.releaseYear}) — {entry.averageRating.toFixed(1)}/10
                    </li>
                ))}
            </ul>
        </div>
    );
}

export default Browse;