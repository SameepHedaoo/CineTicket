import { useEffect, useState } from "react";
import { api } from "../api/api";
import { useNavigate } from "react-router-dom";

function Movies() {
    const [movies, setMovies] = useState([]);
    const [error, setError] = useState(null);
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    useEffect(() => {
        setLoading(true);
        setError(null);

        api.get("/movies")
            .then((res) => {
                const normalizedMovies = (res.data || []).map((movie) => ({
                    id: movie.id ?? movie.movieId,
                    name: movie.name ?? movie.title ?? movie.movieTitle,
                }));
                setMovies(normalizedMovies);
            })
            .catch((err) => {
                console.error("Failed to fetch movies:", err);
                setError("Failed to load movies");
            })
            .finally(() => setLoading(false));
    }, []);

    return (
        <div className="page">
            <div className="page-header">
                <div>
                    <h2>Movies</h2>
                    <p className="subtle">Pick a movie to filter shows by title.</p>
                </div>
            </div>

            {error && <div className="error">{error}</div>}
            {loading && <div className="muted">Loading movies...</div>}

            <div className="card-grid">
                {movies.map((movie) => (
                    <div
                        key={movie.id}
                        className="card clickable"
                        onClick={() =>
                            navigate(`/shows?movie=${encodeURIComponent(movie.name || "")}`)
                        }
                    >
                        <div className="card-title">{movie.name}</div>
                        <div className="card-meta">Tap to view shows</div>
                    </div>
                ))}
            </div>
        </div>
    );
}

export default Movies;
