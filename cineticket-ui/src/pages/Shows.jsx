import { useEffect, useMemo, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { api } from "../api/api";

const CITIES = ["Pune", "Mumbai", "Bangalore"];

function Shows() {
    const [searchParams, setSearchParams] = useSearchParams();
    const navigate = useNavigate();
    const [city, setCity] = useState(searchParams.get("city") || "Pune");
    const [movieFilter, setMovieFilter] = useState(searchParams.get("movie") || "");
    const [shows, setShows] = useState([]);
    const [error, setError] = useState(null);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        const nextParams = new URLSearchParams();
        if (city) {
            nextParams.set("city", city);
        }
        if (movieFilter) {
            nextParams.set("movie", movieFilter);
        }
        setSearchParams(nextParams, { replace: true });
    }, [city, movieFilter, setSearchParams]);

    useEffect(() => {
        if (!city) {
            return;
        }

        setLoading(true);
        setError(null);

        api.get(`/shows?city=${encodeURIComponent(city)}`)
            .then((res) => {
                setShows(res.data || []);
            })
            .catch((err) => {
                console.error("Failed to fetch shows:", err);
                setError("Failed to load shows");
            })
            .finally(() => setLoading(false));
    }, [city]);

    const filteredShows = useMemo(() => {
        if (!movieFilter) {
            return shows;
        }
        const needle = movieFilter.toLowerCase();
        return shows.filter((show) => (show.movieName || "").toLowerCase().includes(needle));
    }, [shows, movieFilter]);

    return (
        <div className="page">
            <div className="page-header">
                <div>
                    <h2>Shows</h2>
                    <p className="subtle">Browse shows by city and optionally filter by movie.</p>
                </div>
                <div className="filters">
                    <select value={city} onChange={(e) => setCity(e.target.value)}>
                        {CITIES.map((item) => (
                            <option key={item} value={item}>
                                {item}
                            </option>
                        ))}
                    </select>
                    <input
                        type="text"
                        placeholder="Filter by movie name"
                        value={movieFilter}
                        onChange={(e) => setMovieFilter(e.target.value)}
                    />
                </div>
            </div>

            {error && <div className="error">{error}</div>}
            {loading && <div className="muted">Loading shows...</div>}

            {!loading && filteredShows.length === 0 && !error && (
                <div className="muted">No shows found for this city.</div>
            )}

            <div className="card-grid">
                {filteredShows.map((show) => (
                    <div key={show.showId} className="card">
                        <div className="card-title">{show.movieName}</div>
                        <div className="card-meta">{show.screenName}</div>
                        <div className="card-meta">
                            Start: {show.startTime ? new Date(show.startTime).toLocaleString() : "TBD"}
                        </div>
                        <div className="card-meta">Price: {show.price}</div>
                        <div className="card-meta">Available seats: {show.availableSeats}</div>
                        <button
                            className="primary"
                            onClick={() => navigate(`/shows/${show.showId}/layout`)}
                        >
                            View Seat Layout
                        </button>
                    </div>
                ))}
            </div>
        </div>
    );
}

export default Shows;
