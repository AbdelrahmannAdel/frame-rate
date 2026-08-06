import { useParams } from "react-router-dom";
import { useAuth } from "../context/AuthContext.jsx";

function Profile() {
    const { id } = useParams();
    const { currentUser, logout } = useAuth();

    const isOwnProfile = currentUser && Number(id) === currentUser.id;

    return (
        <div>
            <h1>Profile page (id: {id})</h1>

            {isOwnProfile && (
                <div className="settings-box">
                    <h2>settings</h2>
                    <button onClick={logout}>log out</button>
                </div>
            )}
        </div>
    );
}

export default Profile;