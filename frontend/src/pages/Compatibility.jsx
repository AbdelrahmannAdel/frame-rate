import { useEffect, useState } from "react";
import { useParams, Link } from "react-router-dom";
import { getCompatibility, getUser } from "../api.js";
import { useAuth } from "../context/AuthContext.jsx";
import StarRating from "../components/StarRating.jsx";

function Compatibility() {
    const { otherId } = useParams();
    const { currentUser } = useAuth();

    const [otherUser, setOtherUser] = useState(null);
    const [result, setResult] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        setLoading(true);
        setError(null);

        Promise.all([getUser(otherId), getCompatibility(otherId)])
            .then(([userData, compatData]) => {
                setOtherUser(userData);
                setResult(compatData);
            })
            .catch((err) => setError(err.message))
            .finally(() => setLoading(false));
    }, [otherId]);

    if (loading) return <p>loading...</p>;
    if (error) return <p className="form-error">{error}</p>;
    if (!result || !otherUser) return null;

    const hasScore = result.compatibilityScore !== null;

    return (
        <div>
            <h1>
                you & <Link to={`/profile/${otherUser.id}`}>{otherUser.username}</Link>
            </h1>

            {hasScore ? (
                <>
                    <p className="compat-score">{result.compatibilityScore.toFixed(1)}% compatible</p>
                    <p className="meta">
                        based on {result.sharedMovies.length} shared movie{result.sharedMovies.length !== 1 ? "s" : ""}
                    </p>

                    <table className="compat-table">
                        <thead>
                        <tr>
                            <th>movie</th>
                            <th>you</th>
                            <th>{otherUser.username}</th>
                        </tr>
                        </thead>
                        <tbody>
                        {result.sharedMovies.map((sm) => (
                            <tr key={sm.movieId}>
                                <td>
                                    <Link to={`/movies/${sm.movieId}`}>{sm.title}</Link>
                                </td>
                                <td>
                                    <StarRating value={sm.myRating} size="small" />
                                </td>
                                <td>
                                    <StarRating value={sm.theirRating} size="small" />
                                </td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </>
            ) : (
                <p>no shared movies yet — you and {otherUser.username} haven't rated anything in common.</p>
            )}
        </div>
    );
}

export default Compatibility;