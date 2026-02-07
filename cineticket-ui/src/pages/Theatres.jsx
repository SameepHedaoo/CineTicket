import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { api } from "../api/api";

const CITY_FALLBACK = [];

function Theatres() {
    const [cities, setCities] = useState(CITY_FALLBACK);
    const [city, setCity] = useState("");
    const [theatres, setTheatres] = useState([]);
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
            return;
        }

        setLoading(true);
        setError(null);

        api.get(`/theatres?city=${encodeURIComponent(city)}`)
            .then((res) => {
                setTheatres(res.data || []);
            })
            .catch((err) => {
                console.error("Failed to fetch theatres:", err);
                setError("Failed to load theatres");
            })
            .finally(() => setLoading(false));
    }, [city]);

    return (
        <div className="page">
            <div className="page-header">
                <div>
                    <h2>Theatres</h2>
                    <p className="subtle">Browse theatres by city.</p>
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
            {loading && <div className="muted">Loading theatres...</div>}

            <div className="card-grid">
                {theatres.map((theatre) => (
                    <div key={theatre.id} className="card">
                        <div className="card-title">{theatre.name}</div>
                        <div className="card-meta">{theatre.address}</div>
                        <div className="card-meta">{theatre.city}</div>
                        <button
                            className="primary"
                            type="button"
                            onClick={() =>
                                navigate(
                                    `/shows?theatreId=${encodeURIComponent(
                                        theatre.id
                                    )}&theatreName=${encodeURIComponent(
                                        theatre.name || ""
                                    )}&city=${encodeURIComponent(city)}`
                                )
                            }
                        >
                            Browse Shows
                        </button>
                    </div>
                ))}
            </div>
        </div>
    );
}

export default Theatres;
