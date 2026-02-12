import { useEffect, useMemo, useState } from "react";
import { API_BASE_URL, api } from "../api/api";
import { useNavigate } from "react-router-dom";

const CITY_FALLBACK = [];

function Movies() {
    const [movies, setMovies] = useState([]); // movies with shows in the selected city
    const [movieCatalog, setMovieCatalog] = useState([]); // /movies metadata (rating, language, etc.)
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
        api.get("/movies")
            .then((res) => setMovieCatalog(res.data || []))
            .catch(() => setMovieCatalog([]));
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
                        const existing = byMovieName.get(name);
                        if (existing) {
                            existing.showCount += 1;
                            if (show.theatreName) {
                                existing.theatres.add(String(show.theatreName));
                            }
                        }
                        continue;
                    }
                    byMovieName.set(name, {
                        id: show.movieId ?? null,
                        name,
                        posterUrl: show.moviePosterUrl || null,
                        showCount: 1,
                        theatres: new Set(show.theatreName ? [String(show.theatreName)] : []),
                    });
                }
                const normalizedMovies = Array.from(byMovieName.values())
                    .map((item) => ({
                        ...item,
                        theatreCount: item.theatres.size,
                    }))
                    .sort((a, b) => a.name.localeCompare(b.name));
                setMovies(normalizedMovies);
            })
            .catch((err) => {
                console.error("Failed to fetch movies:", err);
                setError("Failed to load movies");
            })
            .finally(() => setLoading(false));
    }, [city]);

    const catalogById = useMemo(() => {
        const map = new Map();
        for (const m of movieCatalog || []) {
            if (m?.id !== null && m?.id !== undefined) {
                map.set(String(m.id), m);
            }
        }
        return map;
    }, [movieCatalog]);

    return (
        <div className="page">
            <div className="page-header">
                <div>
                    <h2>Movies</h2>
                    <p className="subtle">Pick a city, then choose a movie.</p>
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
                    (() => {
                        const meta = movie?.id ? catalogById.get(String(movie.id)) : null;
                        const displayTitle = meta?.title || movie.name;
                        const displayLang = meta?.language || null;
                        const displayRating = meta?.rating ?? null;
                        const poster = movie.posterUrl
                            ? (movie.posterUrl.startsWith("http") ? movie.posterUrl : `${API_BASE_URL}${movie.posterUrl}`)
                            : (meta?.posterUrl ? (meta.posterUrl.startsWith("http") ? meta.posterUrl : `${API_BASE_URL}${meta.posterUrl}`) : null);
                        return (
                    <div
                        key={movie.id}
                        className="card clickable"
                        onClick={() =>
                            navigate(`/movies/${encodeURIComponent(movie.id)}?city=${encodeURIComponent(city)}`)
                        }
                    >
                        {poster && (
                            <img
                                className="movie-poster"
                                src={poster}
                                alt={`${displayTitle} poster`}
                                loading="lazy"
                            />
                        )}
                        <div className="card-title">{displayTitle}</div>
                        <div className="card-meta">
                            {displayLang ? displayLang : " "}
                            {displayRating !== null && displayRating !== undefined ? ` • Rating ${displayRating}/10` : ""}
                        </div>
                        <div className="card-meta">
                            {(movie.theatreCount ?? 0) > 0 ? `${movie.theatreCount} theatre${movie.theatreCount === 1 ? "" : "s"}` : " "}
                            {(movie.showCount ?? 0) > 0 ? ` • ${movie.showCount} show${movie.showCount === 1 ? "" : "s"}` : ""}
                        </div>
                    </div>
                        );
                    })()
                ))}
            </div>
        </div>
    );
}

export default Movies;
