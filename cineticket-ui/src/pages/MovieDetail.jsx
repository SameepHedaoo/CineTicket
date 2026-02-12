import { useEffect, useMemo, useState } from "react";
import { useNavigate, useParams, useSearchParams } from "react-router-dom";
import { API_BASE_URL, api } from "../api/api";

const CITY_FALLBACK = [];

function MovieDetail() {
  const { movieId } = useParams();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();

  const [cities, setCities] = useState(CITY_FALLBACK);
  const [city, setCity] = useState(searchParams.get("city") || "");
  const [movie, setMovie] = useState(null);
  const [shows, setShows] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    api
      .get("/cities")
      .then((res) => {
        const list = (res.data || []).map((item) => item.name).filter(Boolean);
        setCities(list);
        if (!city && list.length > 0) {
          setCity(list[0]);
        }
      })
      .catch(() => setCities(CITY_FALLBACK));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    if (!movieId) {
      return;
    }
    setError(null);
    api
      .get("/movies")
      .then((res) => {
        const found = (res.data || []).find((m) => String(m.id) === String(movieId));
        setMovie(found || null);
      })
      .catch((err) => {
        console.error("Failed to load movie details:", err);
        setMovie(null);
      });
  }, [movieId]);

  useEffect(() => {
    if (!movieId || !city) {
      setShows([]);
      return;
    }

    setLoading(true);
    setError(null);
    api
      .get(`/shows?city=${encodeURIComponent(city)}`)
      .then((res) => {
        const list = res.data || [];
        const filtered = list.filter((s) => String(s.movieId) === String(movieId));
        setShows(filtered);
      })
      .catch((err) => {
        console.error("Failed to load shows:", err);
        setError("Failed to load show timings.");
      })
      .finally(() => setLoading(false));
  }, [movieId, city]);

  const posterUrl = useMemo(() => {
    const raw = movie?.posterUrl || null;
    if (!raw) {
      // Fall back: use first show poster.
      const showPoster = shows?.[0]?.moviePosterUrl || null;
      if (!showPoster) {
        return null;
      }
      return showPoster.startsWith("http") ? showPoster : `${API_BASE_URL}${showPoster}`;
    }
    return raw.startsWith("http") ? raw : `${API_BASE_URL}${raw}`;
  }, [movie?.posterUrl, shows]);

  const groupedByTheatre = useMemo(() => {
    const map = new Map();
    for (const show of shows || []) {
      const theatre = (show.theatreName || "Theatre").trim();
      const arr = map.get(theatre) || [];
      arr.push(show);
      map.set(theatre, arr);
    }
    for (const [key, arr] of map.entries()) {
      arr.sort((a, b) => new Date(a.startTime).getTime() - new Date(b.startTime).getTime());
      map.set(key, arr);
    }
    return Array.from(map.entries()).sort((a, b) => a[0].localeCompare(b[0]));
  }, [shows]);
  const theatreCount = groupedByTheatre.length;
  const showCount = shows.length;

  const title = movie?.title || shows?.[0]?.movieName || "Movie";
  const language = movie?.language || null;
  const rating = movie?.rating ?? null;
  const duration = movie?.duration || movie?.durationMinutes || null;
  const genre = movie?.genre || null;
  const description = movie?.description || null;

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <h2 className="hero-title">{title}</h2>
          <p className="subtle">
            {genre ? `${genre} • ` : ""}
            {duration ? `${duration} min • ` : ""}
            {language ? `${language} • ` : ""}
            {rating !== null && rating !== undefined ? `Rating ${rating}/10` : " "}
          </p>
          <p className="subtle">
            {showCount > 0
              ? `${theatreCount} theatre${theatreCount === 1 ? "" : "s"} • ${showCount} show${showCount === 1 ? "" : "s"} available in ${city || "selected city"}`
              : `No shows available in ${city || "selected city"}.`}
          </p>
        </div>
        <div className="filters">
          <select value={city} onChange={(e) => setCity(e.target.value)}>
            {cities.map((item) => (
              <option key={item} value={item}>
                {item}
              </option>
            ))}
          </select>
          <button className="secondary" type="button" onClick={() => navigate(`/shows?city=${encodeURIComponent(city)}`)}>
            View All Shows
          </button>
        </div>
      </div>

      <div className="detail-layout">
        <div className="detail-hero">
          {posterUrl && (
            <img className="detail-poster" src={posterUrl} alt={`${title} poster`} loading="lazy" />
          )}
          {description && (
            <div className="card detail-card">
              <div className="card-title">Synopsis</div>
              <div className="card-meta detail-synopsis">{description}</div>
            </div>
          )}
        </div>

        <div className="detail-shows">
          <div className="card detail-card">
            <div className="card-title">Showtimings</div>
            <div className="card-meta">
              City: <strong>{city || "Select city"}</strong>
            </div>

            {error && <div className="error">{error}</div>}
            {loading && <div className="muted">Loading show timings...</div>}
            {!loading && !error && groupedByTheatre.length === 0 && (
              <div className="muted">No shows available for this movie in {city}.</div>
            )}

            <div className="theatre-groups">
              {groupedByTheatre.map(([theatre, theatreShows]) => (
                <div key={theatre} className="theatre-group">
                  <div className="theatre-group-header">
                    <div className="show-theatre">{theatre}</div>
                    <div className="card-meta">
                      {theatreShows.length} show{theatreShows.length === 1 ? "" : "s"}
                    </div>
                  </div>
                  <div className="showtime-row">
                    {theatreShows.map((s) => (
                      <button
                        key={s.showId}
                        className="showtime-chip"
                        type="button"
                        onClick={() => navigate(`/shows/${s.showId}/layout`)}
                        title={`Select seats - ${theatre}`}
                      >
                        <span className="showtime-time">
                          {s.startTime ? new Date(s.startTime).toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" }) : "TBD"}
                        </span>
                        <span className="showtime-price">
                          {s.price !== null && s.price !== undefined ? `INR ${s.price}` : "TBD"}
                        </span>
                      </button>
                    ))}
                  </div>
                  <button
                    className="primary select-seats-cta"
                    type="button"
                    onClick={() => {
                      const firstShow = theatreShows?.[0];
                      if (!firstShow?.showId) {
                        return;
                      }
                      navigate(`/shows/${firstShow.showId}/layout`);
                    }}
                    disabled={!theatreShows?.length}
                  >
                    Select Seats
                  </button>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default MovieDetail;
