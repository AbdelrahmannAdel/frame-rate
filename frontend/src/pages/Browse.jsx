import { useEffect, useState } from "react";
import { request } from "../api.js";

function Browse() {
    // movies starts as an empty list; setMovies is how we update it
    const [movies, setMovies] = useState([]);

    // runs once, right after this page first loads
    useEffect(() => {
        request("/movies")
            .then((data) => setMovies(data))
            .catch((err) => console.error("Failed to load movies:", err));
    }, []);

    return (
        <div>
            <h1>Browse page</h1>
            <ul>
                {movies.map((movie) => (
                    <li key={movie.id}>
                        {movie.title} ({movie.releaseYear})
                    </li>
                ))}
            </ul>
        </div>
    );
}

export default Browse;