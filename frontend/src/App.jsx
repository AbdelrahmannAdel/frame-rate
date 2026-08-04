import { Routes, Route, Link } from 'react-router-dom'
import Browse from './pages/Browse.jsx'
import Login from './pages/Login.jsx'
import Register from './pages/Register.jsx'
import MovieDetail from './pages/MovieDetail.jsx'
import Profile from './pages/Profile.jsx'
import './style.css'

function App() {
  return (
      <>
        <nav>
          <Link to="/">browse</Link>
          <Link to="/profile">profile</Link>
          <Link to="/login">log in</Link>
          <Link to="/register">register</Link>
        </nav>

        <Routes>
          <Route path="/" element={<Browse />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/movies/:id" element={<MovieDetail />} />
          <Route path="/profile" element={<Profile />} />
        </Routes>
      </>
  )
}

export default App