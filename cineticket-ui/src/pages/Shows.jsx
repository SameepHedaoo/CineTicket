import { useEffect, useMemo, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { API_BASE_URL, api } from "../api/api";

const CITY_FALLBACK = [];
const DATE_FILTERS = [
    { value: "", label: "All" },
    { value: "today", label: "Today" },
    { value: "tomorrow", label: "Tomorrow" },
    { value: "weekend", label: "Weekend" },
];

const PRICE_BUCKETS = [
    { value: "", label: "Any price" },
    { value: "under-200", label: "Under ₹200" },
    { value: "200-400", label: "₹200–₹400" },
    { value: "400-plus", label: "₹400+" },
];

function Shows() {
    const [searchParams, setSearchParams] = useSearchParams();
    const navigate = useNavigate();
    const [cities, setCities] = useState(CITY_FALLBACK);
    const [city, setCity] = useState(searchParams.get("city") || "");
    const [theatreId, setTheatreId] = useState(searchParams.get("theatreId") || "");
    const [theatreName, setTheatreName] = useState(searchParams.get("theatreName") || "");
    const [movieId, setMovieId] = useState(searchParams.get("movieId") || "");
    const [movieFilter, setMovieFilter] = useState(searchParams.get("movie") || "");
    const [dateFilter, setDateFilter] = useState(searchParams.get("date") || "");
    const [priceBucket, setPriceBucket] = useState(searchParams.get("price") || "");
    const [shows, setShows] = useState([]);
    const [error, setError] = useState(null);
    const [loading, setLoading] = useState(false);

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
        const nextParams = new URLSearchParams();
        if (city) {
            nextParams.set("city", city);
        }
        if (theatreId) {
            nextParams.set("theatreId", theatreId);
        }
        if (theatreName) {
            nextParams.set("theatreName", theatreName);
        }
        if (movieId) {
            nextParams.set("movieId", movieId);
        }
        if (movieFilter) {
            nextParams.set("movie", movieFilter);
        }
        if (dateFilter) {
            nextParams.set("date", dateFilter);
        }
        if (priceBucket) {
            nextParams.set("price", priceBucket);
        }
        setSearchParams(nextParams, { replace: true });
    }, [city, theatreId, theatreName, movieId, movieFilter, dateFilter, priceBucket, setSearchParams]);

    useEffect(() => {
        if (!city && !theatreId && !movieId) {
            return;
        }

        setLoading(true);
        setError(null);

        let request;
        if (theatreId) {
            request = api.get(`/shows/theatre/${encodeURIComponent(theatreId)}`);
        } else {
            request = api.get(`/shows?city=${encodeURIComponent(city)}`);
        }

        request
            .then((res) => {
                const list = res.data || [];
                // If we have a movieId and we're browsing by city, filter client-side so the city context is respected.
                if (movieId) {
                    setShows(list.filter((item) => String(item.movieId) === String(movieId)));
                } else {
                    setShows(list);
                }
            })
            .catch((err) => {
                console.error("Failed to fetch shows:", err);
                setError("Failed to load shows");
            })
            .finally(() => setLoading(false));
    }, [city, theatreId, movieId]);

    const movieOptions = useMemo(() => {
        const unique = new Set(
            (shows || [])
                .map((show) => (show.movieName || "").trim())
                .filter(Boolean)
        );
        return Array.from(unique).sort((a, b) => a.localeCompare(b));
    }, [shows]);

    useEffect(() => {
        if (!movieFilter) {
            return;
        }
        if (!movieOptions.includes(movieFilter)) {
            setMovieFilter("");
        }
    }, [movieFilter, movieOptions]);

    const filteredShows = useMemo(() => {
        let next = shows;

        if (movieFilter) {
            next = next.filter((show) => (show.movieName || "") === movieFilter);
        }

        if (dateFilter) {
            const today = new Date();
            const startOfToday = new Date(today.getFullYear(), today.getMonth(), today.getDate());
            const startOfTomorrow = new Date(startOfToday);
            startOfTomorrow.setDate(startOfTomorrow.getDate() + 1);
            const startOfDayAfterTomorrow = new Date(startOfTomorrow);
            startOfDayAfterTomorrow.setDate(startOfDayAfterTomorrow.getDate() + 1);

            next = next.filter((show) => {
                if (!show.startTime) {
                    return false;
                }
                const showDate = new Date(show.startTime);
                if (dateFilter === "today") {
                    return showDate >= startOfToday && showDate < startOfTomorrow;
                }
                if (dateFilter === "tomorrow") {
                    return showDate >= startOfTomorrow && showDate < startOfDayAfterTomorrow;
                }
                if (dateFilter === "weekend") {
                    const day = showDate.getDay();
                    return day === 0 || day === 6;
                }
                return true;
            });
        }

        if (priceBucket) {
            next = next.filter((show) => {
                const price = Number(show.price);
                if (Number.isNaN(price)) {
                    return false;
                }
                if (priceBucket === "under-200") {
                    return price < 200;
                }
                if (priceBucket === "200-400") {
                    return price >= 200 && price <= 400;
                }
                if (priceBucket === "400-plus") {
                    return price > 400;
                }
                return true;
            });
        }

        return next;
    }, [shows, movieFilter, dateFilter, priceBucket]);
    const isSpecificMovieView = Boolean(movieId || movieFilter);
    const isAllShowsOverview = !theatreId && !isSpecificMovieView;

    const overviewMovies = useMemo(() => {
        if (!isAllShowsOverview) {
            return [];
        }
        const map = new Map();
        for (const show of filteredShows || []) {
            const name = String(show.movieName || "").trim();
            if (!name) {
                continue;
            }
            const key = show.movieId !== null && show.movieId !== undefined ? `id:${show.movieId}` : `name:${name.toLowerCase()}`;
            const existing = map.get(key) || {
                movieId: show.movieId ?? null,
                movieName: name,
                posterUrl: show.moviePosterUrl || null,
                showCount: 0,
                theatres: new Set(),
                minPrice: null,
                nextStartTime: null,
            };

            existing.showCount += 1;
            if (show.theatreName) {
                existing.theatres.add(String(show.theatreName));
            }

            const price = show.price !== null && show.price !== undefined ? Number(show.price) : null;
            if (price !== null && !Number.isNaN(price)) {
                existing.minPrice = existing.minPrice === null ? price : Math.min(existing.minPrice, price);
            }

            if (show.startTime) {
                const start = new Date(show.startTime).getTime();
                if (!Number.isNaN(start)) {
                    existing.nextStartTime = existing.nextStartTime === null ? start : Math.min(existing.nextStartTime, start);
                }
            }

            if (!existing.posterUrl && show.moviePosterUrl) {
                existing.posterUrl = show.moviePosterUrl;
            }

            map.set(key, existing);
        }

        return Array.from(map.values())
            .map((item) => ({ ...item, theatreCount: item.theatres.size }))
            .sort((a, b) => a.movieName.localeCompare(b.movieName));
    }, [filteredShows, isAllShowsOverview]);
    const selectedMovieTitle = useMemo(() => {
        if (movieFilter) {
            return movieFilter;
        }
        const fromQuery = (searchParams.get("movie") || "").trim();
        if (fromQuery) {
            return fromQuery;
        }
        if (movieId) {
            const candidate = (shows?.[0]?.movieName || "").trim();
            if (candidate) {
                return candidate;
            }
        }
        return "";
    }, [movieFilter, movieId, shows, searchParams]);

    return (
        <div className="page">
            <div className="page-header">
                <div>
                    <h2 className={isSpecificMovieView ? "hero-title" : ""}>
                        {isSpecificMovieView ? selectedMovieTitle || "Shows" : "Shows"}
                    </h2>
                    <p className="subtle">
                        {theatreId
                            ? `Showing shows for ${theatreName || "selected theatre"}.`
                            : isSpecificMovieView
                                ? "Compare theatres and prices, then pick a showtime."
                                : "Browse shows by city and optionally filter by movie, date, and price."}
                    </p>
                </div>
                <div className="filters">
                    {!theatreId && (
                        <select value={city} onChange={(e) => setCity(e.target.value)}>
                            {cities.map((item) => (
                                <option key={item} value={item}>
                                    {item}
                                </option>
                            ))}
                        </select>
                    )}
                    <div className="filter-tabs" role="group" aria-label="Show date filter">
                        {DATE_FILTERS.map((item) => (
                            <button
                                key={item.value || "all"}
                                type="button"
                                className={`tab ${dateFilter === item.value ? "active" : ""}`}
                                onClick={() => setDateFilter(item.value)}
                            >
                                {item.label}
                            </button>
                        ))}
                    </div>
                    <select value={priceBucket} onChange={(e) => setPriceBucket(e.target.value)}>
                        {PRICE_BUCKETS.map((item) => (
                            <option key={item.value || "any"} value={item.value}>
                                {item.label}
                            </option>
                        ))}
                    </select>
                    <select value={movieFilter} onChange={(e) => setMovieFilter(e.target.value)}>
                        <option value="">All movies</option>
                        {movieOptions.map((movieName) => (
                            <option key={movieName} value={movieName}>
                                {movieName}
                            </option>
                        ))}
                    </select>
                    {theatreId && (
                        <button
                            className="secondary"
                            type="button"
                            onClick={() => {
                                setTheatreId("");
                                setTheatreName("");
                            }}
                        >
                            Clear theatre
                        </button>
                    )}
                    {movieId && (
                        <button
                            className="secondary"
                            type="button"
                            onClick={() => {
                                setMovieId("");
                                setMovieFilter("");
                            }}
                        >
                            Clear movie
                        </button>
                    )}
                </div>
            </div>

            {error && <div className="error">{error}</div>}
            {loading && <div className="muted">Loading shows...</div>}

            {!loading && filteredShows.length === 0 && !error && (
                <div className="muted">No shows found for this city.</div>
            )}

            <div className="card-grid">
                {isAllShowsOverview
                    ? overviewMovies.map((movie) => {
                        const posterUrl = movie.posterUrl
                            ? (movie.posterUrl.startsWith("http") ? movie.posterUrl : `${API_BASE_URL}${movie.posterUrl}`)
                            : null;
                        return (
                            <div
                                key={movie.movieId ?? movie.movieName}
                                className="card clickable"
                                onClick={() => setMovieFilter(movie.movieName)}
                            >
                                {posterUrl && (
                                    <img
                                        className="movie-poster"
                                        src={posterUrl}
                                        alt={`${movie.movieName} poster`}
                                        loading="lazy"
                                    />
                                )}
                                <div className="card-title">{movie.movieName}</div>
                                <div className="card-meta">
                                    {movie.theatreCount} theatre{movie.theatreCount === 1 ? "" : "s"} • {movie.showCount} show{movie.showCount === 1 ? "" : "s"}
                                </div>
                                <div className="card-meta">
                                    {movie.minPrice !== null ? `From INR ${movie.minPrice}` : "Price TBD"}
                                    {movie.nextStartTime !== null
                                        ? ` • Next ${new Date(movie.nextStartTime).toLocaleString()}`
                                        : ""}
                                </div>
                                <button className="primary" type="button" onClick={(e) => { e.stopPropagation(); setMovieFilter(movie.movieName); }}>
                                    See Showtimes
                                </button>
                            </div>
                        );
                    })
                    : filteredShows.map((show) => (
                        <div key={show.showId} className="card">
                            {show.moviePosterUrl && (() => {
                                const posterUrl = show.moviePosterUrl.startsWith("http")
                                    ? show.moviePosterUrl
                                    : `${API_BASE_URL}${show.moviePosterUrl}`;
                                if (isSpecificMovieView) {
                                    return (
                                        <div className="poster-cover">
                                            <div
                                                className="poster-cover-bg"
                                                style={{ backgroundImage: `url(${posterUrl})` }}
                                            />
                                        </div>
                                    );
                                }
                                return (
                                    <img
                                        className="movie-poster"
                                        src={posterUrl}
                                        alt={`${show.movieName || "Movie"} poster`}
                                        loading="lazy"
                                    />
                                );
                            })()}
                            {isSpecificMovieView ? (
                                <div className="show-card-header">
                                    <div className="show-theatre">{show.theatreName || "Theatre"}</div>
                                    <div className="show-price">
                                        {show.price !== null && show.price !== undefined ? `Rs ${show.price}` : "TBD"}
                                    </div>
                                </div>
                            ) : (
                                <div className="card-title">{show.movieName}</div>
                            )}
                            <div className="card-meta">
                                {(show.theatreName ? `${show.theatreName} - ` : "")}{show.screenName}
                            </div>
                            <div className="card-meta">
                                Start: {show.startTime ? new Date(show.startTime).toLocaleString() : "TBD"}
                            </div>
                            {!isSpecificMovieView && (
                                <div className="card-meta">
                                    Base price: {show.price !== null && show.price !== undefined ? `INR ${show.price}` : "TBD"}
                                </div>
                            )}
                            <div className="card-meta">
                                Available seats: {show.availableSeats ?? "TBD"}{" "}
                                {show.totalSeats
                                    ? `/ ${show.totalSeats}`
                                    : ""}
                            </div>
                            {show.totalSeats &&
                                show.availableSeats !== null &&
                                show.availableSeats !== undefined &&
                                show.totalSeats > 0 &&
                                show.availableSeats / show.totalSeats <= 0.2 && (
                                    <div className="badge badge-warning">Almost booked</div>
                                )}
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

