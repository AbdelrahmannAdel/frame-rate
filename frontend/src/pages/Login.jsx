import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { login, getUserIdFromToken, getUser, saveToken } from "../api.js";
import { useAuth } from "../context/AuthContext.jsx";

function Login() {
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState(null);
    const [submitting, setSubmitting] = useState(false);

    const { loginWithToken } = useAuth();
    const navigate = useNavigate();

    async function handleSubmit(e) {
        e.preventDefault();
        setError(null);
        setSubmitting(true);

        try {
            const { token } = await login(email, password);
            saveToken(token);

            const userId = getUserIdFromToken();
            const user = await getUser(userId);

            loginWithToken(token, user);
            navigate(`/profile/${user.id}`);
        } catch (err) {
            setError(err.message);
        } finally {
            setSubmitting(false);
        }
    }

    return (
        <div className="auth-page">
            <h1>log in</h1>

            <form onSubmit={handleSubmit} className="auth-form">
                <label>
                    email
                    <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
                </label>

                <label>
                    password
                    <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} required />
                </label>

                {error && <p className="form-error">{error}</p>}

                <button type="submit" disabled={submitting}>
                    {submitting ? "logging in..." : "log in"}
                </button>
            </form>
        </div>
    );
}

export default Login;