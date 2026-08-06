import { Routes, Route, Link } from 'react-router-dom'
import { useAuth } from './context/AuthContext.jsx'
import Browse from './pages/Browse.jsx'
import Login from './pages/Login.jsx'
import Register from './pages/Register.jsx'
import MovieDetail from './pages/MovieDetail.jsx'
import Profile from './pages/Profile.jsx'
import Compatibility from './pages/Compatibility.jsx'
import './style.css'

function App() {
    const { currentUser, logout } = useAuth()

    return (
        <div className="wrap">
            <nav>
                <Link to="/" className="brand">Frame<span>Rate</span></Link>

                <div className="nav-links">
                    {currentUser === undefined ? null : currentUser ? (
                        <Link to={`/profile/${currentUser.id}`}>{currentUser.username}</Link>
                    ) : (
                        <>
                            <Link to="/login">log in</Link>
                            <Link to="/register">register</Link>
                        </>
                    )}
                </div>
            </nav>

            <Routes>
                <Route path="/" element={<Browse />} />
                <Route path="/login" element={<Login />} />
                <Route path="/register" element={<Register />} />
                <Route path="/movies/:id" element={<MovieDetail />} />
                <Route path="/profile/:id" element={<Profile />} />
                <Route path="/compatibility/:otherId" element={<Compatibility />} />
            </Routes>
        </div>
    )
}

export default App