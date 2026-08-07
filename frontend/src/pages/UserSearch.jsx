import { useState } from "react";
import { Link } from "react-router-dom";
import { searchUsers } from "../api.js";

function UserSearch() {
    const [input, setInput] = useState("");
    const [results, setResults] = useState(null);
    const [searching, setSearching] = useState(false);
    const [error, setError] = useState(null);

    async function handleSubmit(e) {
        e.preventDefault();

        if (!input.trim()) {
            setResults(null);
            return;
        }

        setSearching(true);
        setError(null);

        try {
            const data = await searchUsers(input);
            setResults(data);
        } catch (err) {
            setError(err.message);
        } finally {
            setSearching(false);
        }
    }

    function handleInputChange(e) {
        const value = e.target.value;
        setInput(value);

        if (!value.trim()) {
            setResults(null);
        }
    }

    return (
        <div>
            <h1>find people</h1>

            <form className="search-row" onSubmit={handleSubmit}>
                <input
                    type="text"
                    placeholder="search by username..."
                    value={input}
                    onChange={handleInputChange}
                />
                <button type="submit" disabled={searching}>
                    {searching ? "searching..." : "find it"}
                </button>
            </form>

            {error && <p className="form-error">{error}</p>}

            {results !== null && (
                <ul className="profile-list">
                    {results.length === 0 && <p>no users found.</p>}
                    {results.map((u) => (
                        <li key={u.id}>
                            <Link to={`/profile/${u.id}`}>{u.username}</Link>
                        </li>
                    ))}
                </ul>
            )}
        </div>
    );
}

export default UserSearch;