import { useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { api } from "../api/api";

const REFRESH_INTERVAL_MS = 15000;
const RESERVATION_SECONDS = 5 * 60;

function SeatLayout() {
    const { showId } = useParams();
    const navigate = useNavigate();
    const [layout, setLayout] = useState(null);
    const [error, setError] = useState(null);
    const [notice, setNotice] = useState(null);
    const [loading, setLoading] = useState(false);
    const [selectedSeats, setSelectedSeats] = useState([]);
    const [booking, setBooking] = useState(null);
    const [processing, setProcessing] = useState(false);
    const [lastRefreshAt, setLastRefreshAt] = useState(null);
    const [reservationEndsAt, setReservationEndsAt] = useState(null);
    const [now, setNow] = useState(Date.now());

    const fetchLayout = async ({ silent = false } = {}) => {
        if (!showId) {
            return;
        }
        if (!silent) {
            setLoading(true);
        }
        setError(null);
        if (!silent) {
            setNotice(null);
        }
        try {
            const res = await api.get(`/shows/${showId}/layout`);
            const nextLayout = res.data;
            const availableIds = new Set(
                (nextLayout?.seats || [])
                    .filter((seat) => seat.status === "AVAILABLE")
                    .map((seat) => seat.showSeatId)
            );
            setSelectedSeats((prev) => {
                const next = prev.filter((id) => availableIds.has(id));
                if (next.length < prev.length) {
                    setNotice("Some selected seats were no longer available.");
                }
                return next;
            });
            setLayout(nextLayout);
            setLastRefreshAt(new Date());
        } catch (err) {
            console.error("Failed to fetch seat layout:", err);
            setError("Failed to load seat layout");
        } finally {
            if (!silent) {
                setLoading(false);
            }
        }
    };

    useEffect(() => {
        fetchLayout();
    }, [showId]);

    useEffect(() => {
        if (!showId) {
            return;
        }
        const intervalId = setInterval(() => {
            fetchLayout({ silent: true });
        }, REFRESH_INTERVAL_MS);
        return () => clearInterval(intervalId);
    }, [showId]);

    useEffect(() => {
        if (selectedSeats.length > 0 && !reservationEndsAt) {
            setReservationEndsAt(Date.now() + RESERVATION_SECONDS * 1000);
        }
        if (selectedSeats.length === 0) {
            setReservationEndsAt(null);
        }
    }, [selectedSeats.length, reservationEndsAt]);

    useEffect(() => {
        if (!reservationEndsAt) {
            return;
        }
        const intervalId = setInterval(() => {
            setNow(Date.now());
        }, 1000);
        return () => clearInterval(intervalId);
    }, [reservationEndsAt]);

    useEffect(() => {
        if (!reservationEndsAt) {
            return;
        }
        if (reservationEndsAt - now <= 0) {
            setSelectedSeats([]);
            setReservationEndsAt(null);
            setNotice("Reservation expired. Please select seats again.");
        }
    }, [reservationEndsAt, now]);

    const seats = useMemo(() => {
        const items = layout?.seats || [];
        return [...items].sort((a, b) => (a.seatNumber || 0) - (b.seatNumber || 0));
    }, [layout]);

    const selectedSeatNumbers = useMemo(() => {
        const selected = new Set(selectedSeats);
        return seats
            .filter((seat) => selected.has(seat.showSeatId))
            .map((seat) => seat.seatNumber);
    }, [seats, selectedSeats]);

    const seatPrice = layout?.price;
    const priceNumber = seatPrice !== null && seatPrice !== undefined ? Number(seatPrice) : null;
    const totalPrice =
        priceNumber !== null && selectedSeats.length > 0 ? priceNumber * selectedSeats.length : null;

    const remainingMs = reservationEndsAt ? Math.max(reservationEndsAt - now, 0) : 0;
    const remainingMinutes = Math.floor(remainingMs / 60000);
    const remainingSeconds = Math.floor((remainingMs % 60000) / 1000);
    const formattedRemaining = `${remainingMinutes}:${String(remainingSeconds).padStart(2, "0")}`;

    const toggleSeat = (seat) => {
        if (!seat || seat.status !== "AVAILABLE") {
            return;
        }
        setSelectedSeats((prev) => {
            if (prev.includes(seat.showSeatId)) {
                return prev.filter((id) => id !== seat.showSeatId);
            }
            return [...prev, seat.showSeatId];
        });
    };

    const handleBook = async () => {
        if (!showId) {
            return;
        }
        if (selectedSeats.length === 0) {
            setError("Select at least one available seat.");
            return;
        }
        const token = localStorage.getItem("token");
        if (!token) {
            navigate(`/login?redirect=/shows/${showId}/layout`);
            return;
        }

        setProcessing(true);
        setError(null);

        try {
            const bookingResponse = await api.post("/bookings", {
                showId: Number(showId),
                showSeatIds: selectedSeats,
            });
            const bookingId = bookingResponse.data?.bookingId;
            if (!bookingId) {
                throw new Error("Booking ID missing");
            }

            const paymentResponse = await api.post("/payments/process", {
                bookingId,
                status: "PAID",
            });

            setBooking(paymentResponse.data);
        } catch (err) {
            if (err?.response?.status === 403) {
                navigate(`/login?redirect=/shows/${showId}/layout`);
                return;
            }
            console.error("Booking failed:", err);
            setError("Booking failed. Please try again.");
        } finally {
            setProcessing(false);
        }
    };

    return (
        <div className="page">
            <div className="page-header">
                <div>
                    <h2>Seat Layout</h2>
                    <p className="subtle">
                        {layout?.movieName || "Movie"} • {layout?.theatreName || "Theatre"} •{" "}
                        {layout?.screenName || "Screen"} • Show {layout?.showId || showId}
                    </p>
                    <p className="subtle">
                        Auto-refreshing every {Math.round(REFRESH_INTERVAL_MS / 1000)}s
                        {lastRefreshAt
                            ? ` • Updated ${lastRefreshAt.toLocaleTimeString()}`
                            : ""}
                    </p>
                </div>
            </div>

            {error && <div className="error">{error}</div>}
            {notice && <div className="muted">{notice}</div>}
            {loading && <div className="muted">Loading seat layout...</div>}

            {!loading && seats.length === 0 && !error && (
                <div className="muted">No seats returned for this show.</div>
            )}

            <div className="legend">
                <span className="legend-item available">Available</span>
                <span className="legend-item selected">Selected</span>
                <span className="legend-item locked">Locked</span>
                <span className="legend-item booked">Booked</span>
            </div>

            <div className="seat-layout">
                <div className="seat-grid">
                    {seats.map((seat) => (
                        <div
                            key={seat.showSeatId}
                            className={`seat ${String(seat.status || "").toLowerCase()} ${seat.status === "AVAILABLE" ? "selectable" : ""} ${selectedSeats.includes(seat.showSeatId) ? "selected" : ""}`}
                            title={`Seat ${seat.seatNumber} • ${seat.seatType || "REGULAR"} • ${priceNumber !== null ? `₹${priceNumber}` : "Price TBD"}`}
                            onClick={() => toggleSeat(seat)}
                        >
                            {seat.seatNumber}
                        </div>
                    ))}
                </div>

                <aside className="card summary-card">
                    <div className="card-title">Booking Summary</div>
                    <div className="summary-row">
                        <span>Movie</span>
                        <span>{layout?.movieName || "TBD"}</span>
                    </div>
                    <div className="summary-row">
                        <span>Theatre</span>
                        <span>{layout?.theatreName || "TBD"}</span>
                    </div>
                    <div className="summary-row">
                        <span>Showtime</span>
                        <span>
                            {layout?.startTime
                                ? new Date(layout.startTime).toLocaleString()
                                : "TBD"}
                        </span>
                    </div>
                    <div className="summary-row">
                        <span>Seats</span>
                        <span>{selectedSeatNumbers.length ? selectedSeatNumbers.join(", ") : "None"}</span>
                    </div>
                    <div className="summary-row">
                        <span>Seat price</span>
                        <span>{priceNumber !== null ? `₹${priceNumber.toFixed(2)}` : "TBD"}</span>
                    </div>
                    <div className="summary-row total">
                        <span>Total</span>
                        <span>{totalPrice !== null ? `₹${totalPrice.toFixed(2)}` : "TBD"}</span>
                    </div>

                    {reservationEndsAt && selectedSeats.length > 0 && (
                        <div className="timer">
                            Seats reserved for {formattedRemaining} minutes
                        </div>
                    )}

                    {booking ? (
                        <>
                            <div className="card-meta">Booking ID: {booking.bookingId}</div>
                            <div className="card-meta">Status: {booking.status}</div>
                            <div className="card-meta">Payment: {booking.paymentStatus}</div>
                        </>
                    ) : (
                        <button className="primary" onClick={handleBook} disabled={processing}>
                            {processing ? "Processing..." : "Book & Pay"}
                        </button>
                    )}
                </aside>
            </div>
        </div>
    );
}

export default SeatLayout;
