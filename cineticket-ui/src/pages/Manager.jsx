import { useEffect, useMemo, useRef, useState } from "react";
import { API_BASE_URL, api } from "../api/api";

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

function Manager() {
    const [theatreId, setTheatreId] = useState(null);
    const [role, setRole] = useState(null);
    const [theatre, setTheatre] = useState(null);
    const [movies, setMovies] = useState([]);
    const [shows, setShows] = useState([]);
    const [status, setStatus] = useState(null);
    const [error, setError] = useState(null);

    const [movieForm, setMovieForm] = useState({
        title: "Interstellar",
        description: "Space epic",
        language: "EN",
        durationMinutes: 169,
        genre: "Sci-Fi",
        posterUrl: "",
    });
    const [moviePosterFile, setMoviePosterFile] = useState(null);

    const [screenForm, setScreenForm] = useState({
        name: "Screen 1",
        totalSeats: 30,
    });

    const [showForm, setShowForm] = useState({
        movieId: "",
        screenId: "",
        startTime: "2026-02-05T20:00:00",
        endTime: "2026-02-05T22:30:00",
        price: 250.0,
    });

    const [deleteScreenId, setDeleteScreenId] = useState("");
    const createScreenRef = useRef(null);
    const createShowRef = useRef(null);

    useEffect(() => {
        const token = localStorage.getItem("token");
        const payload = parseJwt(token);
        setRole(payload?.role || null);
        setTheatreId(payload?.theatreId || null);
    }, []);

    const loadData = () => {
        if (!theatreId) {
            return;
        }
        api.get(`/theatres/${theatreId}`)
            .then((res) => setTheatre(res.data))
            .catch(() => setTheatre(null));

        api.get("/movies")
            .then((res) => setMovies(res.data || []))
            .catch(() => setMovies([]));

        api.get(`/shows/theatre/${theatreId}`)
            .then((res) => setShows(res.data || []))
            .catch(() => setShows([]));
    };

    useEffect(() => {
        loadData();
    }, [theatreId]);

    const screens = useMemo(() => theatre?.screens || [], [theatre]);

    const handleSubmit = async (event, action) => {
        event.preventDefault();
        setStatus(null);
        setError(null);
        try {
            const message = await action();
            setStatus(message);
            loadData();
        } catch (err) {
            console.error("Manager action failed:", err);
            const statusCode = err?.response?.status;
            const message = err?.response?.data?.message || err?.response?.data || err?.message;
            setError(
                statusCode
                    ? `Action failed (${statusCode}). ${message || "Check your inputs."}`
                    : `Action failed. ${message || "Check your inputs."}`
            );
        }
    };

    if (!role || role !== "THEATRE_MANAGER") {
        return (
            <div className="page">
                <div className="page-header">
                    <div>
                        <h2>Manager Console</h2>
                        <p className="subtle">This area is only for theatre managers.</p>
                    </div>
                </div>
                <div className="muted">Login with a manager account to continue.</div>
            </div>
        );
    }

    if (!theatreId) {
        return (
            <div className="page">
                <div className="page-header">
                    <div>
                        <h2>Manager Console</h2>
                        <p className="subtle">No theatre assigned to this account.</p>
                    </div>
                </div>
            </div>
        );
    }

    return (
        <div className="page">
            <div className="page-header">
                <div>
                    <h2>Manager Console</h2>
                    <p className="subtle">
                        Theatre: {theatre?.name || "Loading..."} • ID {theatreId}
                    </p>
                </div>
                <div className="filters">
                    <button
                        className="secondary"
                        type="button"
                        onClick={() => createScreenRef.current?.scrollIntoView({ behavior: "smooth" })}
                    >
                        Create Screen
                    </button>
                    <button
                        className="primary"
                        type="button"
                        onClick={() => createShowRef.current?.scrollIntoView({ behavior: "smooth" })}
                    >
                        Create Show
                    </button>
                </div>
            </div>

            <div className="admin-grid">
                <form
                    className="card admin-card"
                    onSubmit={(event) =>
                        handleSubmit(event, async () => {
                            let posterUrl = movieForm.posterUrl || "";
                            if (moviePosterFile) {
                                const formData = new FormData();
                                formData.append("file", moviePosterFile);
                                const uploadResponse = await api.post("/admin/movies/upload-poster", formData, {
                                    headers: { "Content-Type": "multipart/form-data" },
                                    silentSuccessToast: true,
                                });
                                posterUrl = uploadResponse.data?.posterUrl || "";
                            }
                            const response = await api.post("/admin/movies/add", {
                                ...movieForm,
                                posterUrl,
                            });
                            setMoviePosterFile(null);
                            setMovieForm((prev) => ({ ...prev, posterUrl }));
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
                            onChange={(e) =>
                                setMovieForm({ ...movieForm, description: e.target.value })
                            }
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
                                setMovieForm({
                                    ...movieForm,
                                    durationMinutes: Number(e.target.value),
                                })
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
                    <div className="field">
                        <span>Poster</span>
                        <input
                            type="file"
                            accept="image/*"
                            onChange={(e) => setMoviePosterFile(e.target.files?.[0] || null)}
                        />
                        <span className="muted">
                            {moviePosterFile
                                ? `Selected: ${moviePosterFile.name}`
                                : movieForm.posterUrl
                                    ? `Using uploaded poster: ${API_BASE_URL}${movieForm.posterUrl}`
                                    : "Optional image, max 5 MB."}
                        </span>
                    </div>
                    <button className="primary" type="submit">
                        Create Movie
                    </button>
                </form>

                <form
                    className="card admin-card"
                    ref={createScreenRef}
                    onSubmit={(event) =>
                        handleSubmit(event, async () => {
                            const response = await api.post("/theatres/screens", {
                                theatreId: Number(theatreId),
                                name: screenForm.name,
                                totalSeats: Number(screenForm.totalSeats),
                            });
                            return `Screen created: ${response.data?.id}`;
                        })
                    }
                >
                    <div className="card-title">Create Screen</div>
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
                    ref={createShowRef}
                    onSubmit={(event) =>
                        handleSubmit(event, async () => {
                            const response = await api.post("/admin/shows/add", {
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

                <form
                    className="card admin-card"
                    onSubmit={(event) =>
                        handleSubmit(event, async () => {
                            if (!deleteScreenId) {
                                throw new Error("Select a screen to delete.");
                            }
                            await api.delete(`/theatres/screens/${Number(deleteScreenId)}`);
                            setDeleteScreenId("");
                            return "Screen deleted.";
                        })
                    }
                >
                    <div className="card-title">Delete Screen</div>
                    <div className="field">
                        <span>Screen</span>
                        <select
                            value={deleteScreenId}
                            onChange={(e) => setDeleteScreenId(e.target.value)}
                        >
                            <option value="">Select screen</option>
                            {screens.map((screen) => (
                                <option key={screen.id} value={screen.id}>
                                    {screen.name || screen.screenName}
                                </option>
                            ))}
                        </select>
                    </div>
                    <button className="primary" type="submit">
                        Delete Screen
                    </button>
                </form>
            </div>

            {status && <div className="muted">{status}</div>}
            {error && <div className="error">{error}</div>}
        </div>
    );
}

export default Manager;
