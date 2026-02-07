import { useEffect, useMemo, useState } from "react";
import { adminApi, api } from "../api/api";

const CITY_FALLBACK = [];

function Admin() {
    const [adminEmail, setAdminEmail] = useState("admin@example.com");
    const [adminPassword, setAdminPassword] = useState("Admin@123");
    const [authMessage, setAuthMessage] = useState(null);
    const [authError, setAuthError] = useState(null);

    const [movieForm, setMovieForm] = useState({
        title: "Interstellar",
        description: "Space epic",
        language: "EN",
        durationMinutes: 169,
        genre: "Sci-Fi",
    });

    const [deleteMovieId, setDeleteMovieId] = useState("");
    const [deleteTheatreId, setDeleteTheatreId] = useState("");
    const [managerEmail, setManagerEmail] = useState("");
    const [managerTheatreId, setManagerTheatreId] = useState("");

    const [theatreForm, setTheatreForm] = useState({
        name: "Inox Mall",
        city: "",
        address: "Main Road",
    });

    const [screenForm, setScreenForm] = useState({
        theatreId: "",
        name: "Screen 1",
        totalSeats: 30,
    });

    const [showForm, setShowForm] = useState({
        movieId: "",
        screenId: "",
        startTime: "2026-02-04T20:00:00",
        endTime: "2026-02-04T22:45:00",
        price: 250.0,
    });

    const [theatres, setTheatres] = useState([]);
    const [movies, setMovies] = useState([]);
    const [cities, setCities] = useState(CITY_FALLBACK);
    const [city, setCity] = useState("");
    const [status, setStatus] = useState(null);
    const [error, setError] = useState(null);
    const [adminToken, setAdminToken] = useState(localStorage.getItem("adminToken"));

    const loadCities = () => {
        api.get("/cities")
            .then((res) => {
                const list = (res.data || []).map((item) => item.name).filter(Boolean);
                setCities(list);
                if (list.length > 0 && !list.includes(city)) {
                    setCity(list[0]);
                }
                if (list.length > 0) {
                    setTheatreForm((prev) =>
                        list.includes(prev.city) ? prev : { ...prev, city: list[0] }
                    );
                }
            })
            .catch(() => setCities(CITY_FALLBACK));
    };

    const loadReferenceData = () => {
        api.get("/movies")
            .then((res) => setMovies(res.data || []))
            .catch(() => setMovies([]));

        if (city) {
            api.get(`/theatres?city=${encodeURIComponent(city)}`)
                .then((res) => setTheatres(res.data || []))
                .catch(() => setTheatres([]));
        } else {
            setTheatres([]);
        }
    };

    useEffect(() => {
        loadCities();
        loadReferenceData();
    }, [city]);

    const screens = useMemo(() => {
        const allScreens = theatres.flatMap((theatre) => theatre.screens || []);
        return allScreens.map((screen) => ({
            ...screen,
            theatreId: screen.theatreId || screenForm.theatreId,
        }));
    }, [theatres, screenForm.theatreId]);

    const handleAdminLogin = async (event) => {
        event.preventDefault();
        setAuthError(null);
        setAuthMessage(null);
        try {
            const response = await api.post("/auth/login", {
                email: adminEmail,
                password: adminPassword,
            });
            const token = response.data?.token;
            if (!token) {
                throw new Error("Token missing");
            }
            localStorage.setItem("token", token);
            localStorage.setItem("adminToken", token);
            setAdminToken(token);
            window.dispatchEvent(new Event("auth-changed"));
            setAuthMessage("Admin authenticated.");
        } catch (err) {
            console.error("Admin login failed:", err);
            const message = err?.response?.data?.message || err?.message || "Admin login failed";
            setAuthError(message);
        }
    };

    const handleSubmit = async (event, action) => {
        event.preventDefault();
        setStatus(null);
        setError(null);

        if (!adminToken) {
            setError("Admin token missing. Please login as admin first.");
            return;
        }

        try {
            const result = await action();
            setStatus(result);
            loadReferenceData();
        } catch (err) {
            console.error("Admin action failed:", err);
            const statusCode = err?.response?.status;
            const message = err?.response?.data?.message || err?.response?.data || err?.message;
            setError(
                statusCode
                    ? `Action failed (${statusCode}). ${message || "Check your admin token and inputs."}`
                    : `Action failed. ${message || "Check your admin token and inputs."}`
            );
        }
    };

    return (
        <div className="page">
            <div className="page-header">
                <div>
                    <h2>Admin Console</h2>
                    <p className="subtle">Create movies, theatres, screens, and shows.</p>
                </div>
            </div>

            <form className="card admin-card" onSubmit={handleAdminLogin}>
                <div className="card-title">Admin Login</div>
                <div className="field">
                    <span>Email</span>
                    <input
                        type="email"
                        value={adminEmail}
                        onChange={(e) => setAdminEmail(e.target.value)}
                        required
                    />
                </div>
                <div className="field">
                    <span>Password</span>
                    <input
                        type="password"
                        value={adminPassword}
                        onChange={(e) => setAdminPassword(e.target.value)}
                        required
                    />
                </div>
                <button className="primary" type="submit">
                    Login as Admin
                </button>
                {authMessage && <div className="muted">{authMessage}</div>}
                {authError && <div className="error">{authError}</div>}
            </form>

            <div className="admin-grid">
                <form
                    className="card admin-card"
                    onSubmit={(event) =>
                        handleSubmit(event, async () => {
                            const response = await adminApi.post("/admin/movies/add", movieForm);
                            return `Movie created: ${response.data?.id || response.data?.movieId}`;
                        })
                    }
                >
                    <div className="card-title">Create Movie</div>
                    <div className="field">
                        <span>Title</span>
                        <input
                            value={movieForm.title}
                            onChange={(e) => setMovieForm({ ...movieForm, title: e.target.value })}
                        />
                    </div>
                    <div className="field">
                        <span>Description</span>
                        <input
                            value={movieForm.description}
                            onChange={(e) => setMovieForm({ ...movieForm, description: e.target.value })}
                        />
                    </div>
                    <div className="field">
                        <span>Language</span>
                        <input
                            value={movieForm.language}
                            onChange={(e) => setMovieForm({ ...movieForm, language: e.target.value })}
                        />
                    </div>
                    <div className="field">
                        <span>Duration (minutes)</span>
                        <input
                            type="number"
                            value={movieForm.durationMinutes}
                            onChange={(e) =>
                                setMovieForm({ ...movieForm, durationMinutes: Number(e.target.value) })
                            }
                        />
                    </div>
                    <div className="field">
                        <span>Genre</span>
                        <input
                            value={movieForm.genre}
                            onChange={(e) => setMovieForm({ ...movieForm, genre: e.target.value })}
                        />
                    </div>
                    <button className="primary" type="submit">
                        Create Movie
                    </button>
                </form>

                <form
                    className="card admin-card"
                    onSubmit={(event) =>
                        handleSubmit(event, async () => {
                            if (!deleteMovieId) {
                                throw new Error("Select a movie to delete.");
                            }
                            await adminApi.delete(`/admin/movies/${Number(deleteMovieId)}`);
                            setDeleteMovieId("");
                            return "Movie deleted.";
                        })
                    }
                >
                    <div className="card-title">Delete Movie</div>
                    <div className="field">
                        <span>Movie</span>
                        <select
                            value={deleteMovieId}
                            onChange={(e) => setDeleteMovieId(e.target.value)}
                        >
                            <option value="">Select movie</option>
                            {movies.map((movie) => (
                                <option key={movie.id || movie.movieId} value={movie.id || movie.movieId}>
                                    {movie.title || movie.name || movie.movieTitle}
                                </option>
                            ))}
                        </select>
                    </div>
                    <button className="primary" type="submit">
                        Delete Movie
                    </button>
                </form>

                <form
                    className="card admin-card"
                    onSubmit={(event) =>
                        handleSubmit(event, async () => {
                            if (!managerEmail || !managerTheatreId) {
                                throw new Error("Provide manager email and theatre.");
                            }
                            await adminApi.post("/admin/theatre-managers", {
                                email: managerEmail,
                                theatreId: Number(managerTheatreId),
                            });
                            setManagerEmail("");
                            setManagerTheatreId("");
                            return "Theatre manager assigned.";
                        })
                    }
                >
                    <div className="card-title">Assign Theatre Manager</div>
                    <div className="field">
                        <span>Manager Email</span>
                        <input
                            type="email"
                            value={managerEmail}
                            onChange={(e) => setManagerEmail(e.target.value)}
                            placeholder="manager@example.com"
                            required
                        />
                    </div>
                    <div className="field">
                        <span>Filter theatres by city</span>
                        <select value={city} onChange={(e) => setCity(e.target.value)}>
                            {cities.map((item) => (
                                <option key={item} value={item}>
                                    {item}
                                </option>
                            ))}
                        </select>
                    </div>
                    <div className="field">
                        <span>Theatre</span>
                        <select
                            value={managerTheatreId}
                            onChange={(e) => setManagerTheatreId(e.target.value)}
                            required
                        >
                            <option value="">Select theatre</option>
                            {theatres.map((theatre) => (
                                <option key={theatre.id} value={theatre.id}>
                                    {theatre.name}
                                </option>
                            ))}
                        </select>
                    </div>
                    <button className="primary" type="submit">
                        Assign Manager
                    </button>
                </form>

                <form
                    className="card admin-card"
                    onSubmit={(event) =>
                        handleSubmit(event, async () => {
                            const response = await adminApi.post("/theatres/add", theatreForm);
                            setCity(theatreForm.city);
                            return `Theatre created: ${response.data?.id}`;
                        })
                    }
                >
                    <div className="card-title">Create Theatre</div>
                    <div className="field">
                        <span>Name</span>
                        <input
                            value={theatreForm.name}
                            onChange={(e) => setTheatreForm({ ...theatreForm, name: e.target.value })}
                        />
                    </div>
                    <div className="field">
                        <span>City</span>
                        <select
                            value={theatreForm.city}
                            onChange={(e) => setTheatreForm({ ...theatreForm, city: e.target.value })}
                        >
                            {cities.map((item) => (
                                <option key={item} value={item}>
                                    {item}
                                </option>
                            ))}
                        </select>
                    </div>
                    <div className="field">
                        <span>Address</span>
                        <input
                            value={theatreForm.address}
                            onChange={(e) => setTheatreForm({ ...theatreForm, address: e.target.value })}
                        />
                    </div>
                    <button className="primary" type="submit">
                        Create Theatre
                    </button>
                </form>

                <form
                    className="card admin-card"
                    onSubmit={(event) =>
                        handleSubmit(event, async () => {
                            if (!deleteTheatreId) {
                                throw new Error("Select a theatre to delete.");
                            }
                            await adminApi.delete(`/theatres/${Number(deleteTheatreId)}`);
                            setDeleteTheatreId("");
                            return "Theatre deleted.";
                        })
                    }
                >
                    <div className="card-title">Delete Theatre</div>
                    <div className="field">
                        <span>Filter theatres by city</span>
                        <select value={city} onChange={(e) => setCity(e.target.value)}>
                            {cities.map((item) => (
                                <option key={item} value={item}>
                                    {item}
                                </option>
                            ))}
                        </select>
                    </div>
                    <div className="field">
                        <span>Theatre</span>
                        <select
                            value={deleteTheatreId}
                            onChange={(e) => setDeleteTheatreId(e.target.value)}
                        >
                            <option value="">Select theatre</option>
                            {theatres.map((theatre) => (
                                <option key={theatre.id} value={theatre.id}>
                                    {theatre.name}
                                </option>
                            ))}
                        </select>
                    </div>
                    <button className="primary" type="submit">
                        Delete Theatre
                    </button>
                </form>

                <form
                    className="card admin-card"
                    onSubmit={(event) =>
                        handleSubmit(event, async () => {
                            const response = await adminApi.post("/theatres/screens", {
                                ...screenForm,
                                theatreId: Number(screenForm.theatreId),
                                totalSeats: Number(screenForm.totalSeats),
                            });
                            return `Screen created: ${response.data?.id}`;
                        })
                    }
                >
                    <div className="card-title">Create Screen</div>
                    <div className="field">
                        <span>Filter theatres by city</span>
                        <select value={city} onChange={(e) => setCity(e.target.value)}>
                            {cities.map((item) => (
                                <option key={item} value={item}>
                                    {item}
                                </option>
                            ))}
                        </select>
                    </div>
                    <div className="field">
                        <span>Theatre</span>
                        <select
                            value={screenForm.theatreId}
                            onChange={(e) => setScreenForm({ ...screenForm, theatreId: e.target.value })}
                        >
                            <option value="">Select theatre</option>
                            {theatres.map((theatre) => (
                                <option key={theatre.id} value={theatre.id}>
                                    {theatre.name}
                                </option>
                            ))}
                        </select>
                    </div>
                    <div className="field">
                        <span>Screen Name</span>
                        <input
                            value={screenForm.name}
                            onChange={(e) => setScreenForm({ ...screenForm, name: e.target.value })}
                        />
                    </div>
                    <div className="field">
                        <span>Total Seats</span>
                        <input
                            type="number"
                            value={screenForm.totalSeats}
                            onChange={(e) =>
                                setScreenForm({ ...screenForm, totalSeats: Number(e.target.value) })
                            }
                        />
                    </div>
                    <button className="primary" type="submit">
                        Create Screen
                    </button>
                </form>

                <form
                    className="card admin-card"
                    onSubmit={(event) =>
                        handleSubmit(event, async () => {
                            const response = await adminApi.post("/admin/shows/add", {
                                ...showForm,
                                movieId: Number(showForm.movieId),
                                screenId: Number(showForm.screenId),
                                price: Number(showForm.price),
                            });
                            return `Show created: ${response.data?.showId || response.data?.id}`;
                        })
                    }
                >
                    <div className="card-title">Create Show</div>
                    <div className="field">
                        <span>Movie</span>
                        <select
                            value={showForm.movieId}
                            onChange={(e) => setShowForm({ ...showForm, movieId: e.target.value })}
                        >
                            <option value="">Select movie</option>
                            {movies.map((movie) => (
                                <option key={movie.id || movie.movieId} value={movie.id || movie.movieId}>
                                    {movie.title || movie.name || movie.movieTitle}
                                </option>
                            ))}
                        </select>
                    </div>
                    <div className="field">
                        <span>Filter theatres by city</span>
                        <select value={city} onChange={(e) => setCity(e.target.value)}>
                            {cities.map((item) => (
                                <option key={item} value={item}>
                                    {item}
                                </option>
                            ))}
                        </select>
                    </div>
                    <div className="field">
                        <span>Screen</span>
                        <select
                            value={showForm.screenId}
                            onChange={(e) => setShowForm({ ...showForm, screenId: e.target.value })}
                        >
                            <option value="">Select screen</option>
                            {screens.map((screen) => (
                                <option key={screen.id} value={screen.id}>
                                    {screen.name || screen.screenName}
                                </option>
                            ))}
                        </select>
                    </div>
                    <div className="field">
                        <span>Start Time</span>
                        <input
                            type="datetime-local"
                            value={showForm.startTime}
                            onChange={(e) => setShowForm({ ...showForm, startTime: e.target.value })}
                        />
                    </div>
                    <div className="field">
                        <span>End Time</span>
                        <input
                            type="datetime-local"
                            value={showForm.endTime}
                            onChange={(e) => setShowForm({ ...showForm, endTime: e.target.value })}
                        />
                    </div>
                    <div className="field">
                        <span>Price</span>
                        <input
                            type="number"
                            value={showForm.price}
                            onChange={(e) => setShowForm({ ...showForm, price: Number(e.target.value) })}
                        />
                    </div>
                    <button className="primary" type="submit">
                        Create Show
                    </button>
                </form>
            </div>

            {status && <div className="muted">{status}</div>}
            {error && <div className="error">{error}</div>}
        </div>
    );
}

export default Admin;
