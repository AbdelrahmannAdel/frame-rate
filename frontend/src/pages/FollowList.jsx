import { useEffect, useState } from "react";
import { useParams, useNavigate, Link } from "react-router-dom";
import { getUser, getFollowing, getFollowers, followUser, unfollowUser } from "../api.js";
import { useAuth } from "../context/AuthContext.jsx";

function FollowList({ type }) {
    const { id } = useParams();
    const { currentUser } = useAuth();
    const navigate = useNavigate();

    const [profileUser, setProfileUser] = useState(null);
    const [users, setUsers] = useState([]);
    const [myFollowingIds, setMyFollowingIds] = useState(new Set());
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [busyId, setBusyId] = useState(null);

    const fetchList = type === "following" ? getFollowing : getFollowers;

    useEffect(() => {
        setLoading(true);
        setError(null);

        const requests = [getUser(id), fetchList(id)];
        if (currentUser) requests.push(getFollowing(currentUser.id));

        Promise.all(requests)
            .then(([userData, listData, myFollowingData]) => {
                setProfileUser(userData);
                setUsers(listData);
                if (myFollowingData) {
                    setMyFollowingIds(new Set(myFollowingData.map((u) => u.id)));
                }
            })
            .catch((err) => setError(err.message))
            .finally(() => setLoading(false));
    }, [id, type, currentUser]);

    async function handleToggle(targetId) {
        if (!currentUser) {
            navigate("/login");
            return;
        }

        setBusyId(targetId);
        setError(null);

        try {
            if (myFollowingIds.has(targetId)) {
                await unfollowUser(targetId);
                setMyFollowingIds((prev) => {
                    const next = new Set(prev);
                    next.delete(targetId);
                    return next;
                });
            } else {
                await followUser(targetId);
                setMyFollowingIds((prev) => new Set(prev).add(targetId));
            }
        } catch (err) {
            setError(err.message);
        } finally {
            setBusyId(null);
        }
    }

    if (loading) return <p>loading...</p>;
    if (!profileUser) return <p>user not found.</p>;

    return (
        <div>
            <p className="ledger-line">
                <Link to={`/profile/${id}`}>&larr; back to {profileUser.username}'s profile</Link>
            </p>

            <h1>{profileUser.username} — {type}</h1>

            {error && <p className="form-error">{error}</p>}

            {users.length === 0 && <p>no {type} yet.</p>}

            <ul className="profile-list">
                {users.map((u) => (
                    <li key={u.id}>
                        <Link to={`/profile/${u.id}`}>{u.username}</Link>

                        {currentUser && u.id !== currentUser.id && (
                            <button
                                className="remove-button"
                                onClick={() => handleToggle(u.id)}
                                disabled={busyId === u.id}
                            >
                                {myFollowingIds.has(u.id) ? "unfollow" : "follow"}
                            </button>
                        )}
                    </li>
                ))}
            </ul>
        </div>
    );
}

export default FollowList;