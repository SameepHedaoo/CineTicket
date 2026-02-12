import { useEffect, useState } from "react";
import { API_BASE_URL, api } from "../api/api";
import { useNavigate } from "react-router-dom";

const CITY_FALLBACK = [];

function Movies() {
    const [movies, setMovies] = useState([]);
    const [cities, setCities] = useState(CITY_FALLBACK);
    const [city, setCity] = useState("");
    const [error, setError] = useState(null);
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    useEffect(() => {
        api.get("/cities")
            .then((res) => {
                const list = (res.data || []).map((item) => item.name).filter(Boolean);
                setCities(list);
                if (list.length > 0 && !list.includes(city)) {
                    setCity(list[0]);
                }
            })
            .catch(() => {
                setCities(CITY_FALLBACK);
            });
    }, []);

    useEffect(() => {
        if (!city) {
            setMovies([]);
            return;
        }

        setLoading(true);
        setError(null);

        api.get(`/shows?city=${encodeURIComponent(city)}`)
            .then((res) => {
                const byMovieName = new Map();
                for (const show of (res.data || [])) {
                    const name = (show.movieName || "").trim();
                    if (!name || byMovieName.has(name)) {
                        continue;
                    }
                    byMovieName.set(name, {
                        id: show.movieId ?? null,
                        name,
                        posterUrl: show.moviePosterUrl || null,
                    });
                }
                const normalizedMovies = Array.from(byMovieName.values())
                    .sort((a, b) => a.name.localeCompare(b.name));
                setMovies(normalizedMovies);
            })
            .catch((err) => {
                console.error("Failed to fetch movies:", err);
                setError("Failed to load movies");
            })
            .finally(() => setLoading(false));
    }, [city]);

    return (
        <div className="page">
            <div className="page-header">
                <div>
                    <h2>Movies</h2>
                    <p className="subtle">Pick city first, then select a movie.</p>
                </div>
                <div className="filters">
                    <select value={city} onChange={(e) => setCity(e.target.value)}>
                        {cities.map((item) => (
                            <option key={item} value={item}>
                                {item}
                            </option>
                        ))}
                    </select>
                </div>
            </div>

            {error && <div className="error">{error}</div>}
            {loading && <div className="muted">Loading movies...</div>}
            {!loading && !error && city && movies.length === 0 && (
                <div className="muted">No movies found for {city}.</div>
            )}

            <div className="card-grid">
                {movies.map((movie) => (
                    <div
                        key={movie.id}
                        className="card clickable"
                        onClick={() =>
                            navigate(
                                `/shows?city=${encodeURIComponent(city)}${movie.id ? `&movieId=${encodeURIComponent(movie.id)}` : ""}&movie=${encodeURIComponent(movie.name || "")}`
                            )
                        }
                    >
                        {movie.posterUrl && (
                            <img
                                className="movie-poster"
                                src={movie.posterUrl.startsWith("http") ? movie.posterUrl : `${API_BASE_URL}${movie.posterUrl}`}
                                alt={`${movie.name} poster`}
                                loading="lazy"
                            />
                        )}
                        <div className="card-title">{movie.name}</div>
                        <div className="card-meta">Tap to view shows</div>
                    </div>
                ))}
            </div>
        </div>
    );
}

export default Movies;
