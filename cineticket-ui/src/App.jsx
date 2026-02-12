import { BrowserRouter, Routes, Route, NavLink, Navigate } from "react-router-dom";
import { useEffect, useState } from "react";
import Movies from "./pages/Movies";
import MovieDetail from "./pages/MovieDetail";
import Theatres from "./pages/Theatres";
import Shows from "./pages/Shows";
import SeatLayout from "./pages/SeatLayout";
import Auth from "./pages/Auth";
import Admin from "./pages/Admin";
import Manager from "./pages/Manager";
import MyBookings from "./pages/MyBookings";
import { notifySuccess } from "./ui/notificationBus";
import "./App.css";

const parseJwt = (token) => {
  if (!token) {
    return null;
  }
  try {
    const payload = token.split(".")[1];
    const base64 = payload.replace(/-/g, "+").replace(/_/g, "/");
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split("")
        .map((c) => `%${("00" + c.charCodeAt(0).toString(16)).slice(-2)}`)
        .join("")
    );
    return JSON.parse(jsonPayload);
  } catch (err) {
    return null;
  }
};

function App() {
  const [token, setToken] = useState(localStorage.getItem("token"));
  const [role, setRole] = useState(() => parseJwt(localStorage.getItem("token"))?.role || null);

  useEffect(() => {
    const updateToken = () => {
      const nextToken = localStorage.getItem("token");
      setToken(nextToken);
      setRole(parseJwt(nextToken)?.role || null);
    };
    window.addEventListener("auth-changed", updateToken);
    window.addEventListener("storage", updateToken);
    return () => {
      window.removeEventListener("auth-changed", updateToken);
      window.removeEventListener("storage", updateToken);
    };
  }, []);

  const handleLogout = () => {
    localStorage.removeItem("token");
    window.dispatchEvent(new Event("auth-changed"));
    notifySuccess("Logged out successfully.");
  };

  const dashboardElement = () => {
    if (!token) {
      return <Navigate to="/login?redirect=/dashboard" replace />;
    }
    if (role === "ADMIN") {
      return <Admin />;
    }
    if (role === "THEATRE_MANAGER") {
      return <Manager />;
    }
    return <Navigate to="/movies" replace />;
  };

  return (
    <BrowserRouter>
      <div className="app-shell">
        <header className="app-header">
          <div className="brand">CineTicket</div>
          <nav className="nav">
            <NavLink to="/movies" className="nav-link">
              Movies
            </NavLink>
            <NavLink to="/theatres" className="nav-link">
              Theatres
            </NavLink>
            <NavLink to="/shows" className="nav-link">
              Shows
            </NavLink>
            {token && (
              <NavLink to="/my-bookings" className="nav-link">
                My Bookings
              </NavLink>
            )}
            {token && role && role !== "USER" && (
              <NavLink to="/dashboard" className="nav-link">
                Dashboard
              </NavLink>
            )}
            {token ? (
              <button className="secondary" type="button" onClick={handleLogout}>
                Logout
              </button>
            ) : (
              <NavLink to="/login" className="nav-link nav-cta">
                Login
              </NavLink>
            )}
          </nav>
        </header>
        <main className="app-content">
          <Routes>
            <Route path="/" element={<Movies />} />
            <Route path="/movies" element={<Movies />} />
            <Route path="/movies/:movieId" element={<MovieDetail />} />
            <Route path="/theatres" element={<Theatres />} />
            <Route path="/shows" element={<Shows />} />
            <Route path="/shows/:showId/layout" element={<SeatLayout />} />
            <Route path="/my-bookings" element={<MyBookings />} />
            <Route path="/login" element={<Auth />} />
            <Route path="/dashboard" element={dashboardElement()} />
            <Route path="/admin" element={<Admin />} />
            <Route path="/manager" element={<Manager />} />
          </Routes>
        </main>
      </div>
    </BrowserRouter>
  );
}

export default App;
