import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { register } from "../api.js";
import { useAuth } from "../context/AuthContext.jsx";

function Register() {
    const [username, setUsername] = useState("");
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
            const data = await register(username, email, password);
            loginWithToken(data.token, data.user);
            navigate(`/profile/${data.user.id}`);
        } catch (err) {
            setError(err.message);
        } finally {
            setSubmitting(false);
        }
    }

    return (
        <div className="auth-page">
            <h1>register</h1>

            <form onSubmit={handleSubmit} className="auth-form">
                <label>
                    username
                    <input type="text" value={username} onChange={(e) => setUsername(e.target.value)} required />
                </label>

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
                    {submitting ? "creating account..." : "create account"}
                </button>
            </form>
        </div>
    );
}

export default Register;