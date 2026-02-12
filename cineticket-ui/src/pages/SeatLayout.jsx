import { useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { api } from "../api/api";

const REFRESH_INTERVAL_MS = 15000;
const RESERVATION_SECONDS = 5 * 60;
const ROW_SIZE = 10;
const SEAT_TYPE_MULTIPLIERS = {
    REGULAR: 1.0,
    SILVER: 1.15,
    GOLD: 1.3,
    PREMIUM: 1.5,
    VIP: 2.0,
};

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
    const [zoom, setZoom] = useState(1);

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

    const rows = useMemo(() => {
        const grouped = [];
        for (let i = 0; i < seats.length; i += ROW_SIZE) {
            grouped.push(seats.slice(i, i + ROW_SIZE));
        }
        return grouped;
    }, [seats]);

    const getRowLabel = (index) => {
        const first = Math.floor(index / 26);
        const second = index % 26;
        const base = String.fromCharCode(65 + second);
        return first > 0 ? `${String.fromCharCode(64 + first)}${base}` : base;
    };

    const selectedSeatNumbers = useMemo(() => {
        const selected = new Set(selectedSeats);
        return seats
            .filter((seat) => selected.has(seat.showSeatId))
            .map((seat) => seat.seatNumber);
    }, [seats, selectedSeats]);

    const seatPrice = layout?.price;
    const priceNumber = seatPrice !== null && seatPrice !== undefined ? Number(seatPrice) : null;
    const seatPriceForType = (seatType) => {
        if (priceNumber === null) {
            return null;
        }
        const multiplier =
            SEAT_TYPE_MULTIPLIERS[String(seatType || "REGULAR").toUpperCase()] ?? 1.0;
        return priceNumber * multiplier;
    };

    const seatTypePrices = useMemo(() => {
        if (!seats.length) {
            return [];
        }
        const types = Array.from(
            new Set(seats.map((seat) => String(seat.seatType || "REGULAR").toUpperCase()))
        );
        return types.map((type) => ({
            type,
            price: seatPriceForType(type),
        }));
    }, [seats, priceNumber]);

    const totalPrice = selectedSeats.reduce((sum, id) => {
        const seat = seats.find((item) => item.showSeatId === id);
        const priceForSeat = seatPriceForType(seat?.seatType);
        return sum + (priceForSeat ?? 0);
    }, 0);

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

    const loadRazorpayScript = () =>
        new Promise((resolve) => {
            if (window.Razorpay) {
                resolve(true);
                return;
            }
            const script = document.createElement("script");
            script.src = "https://checkout.razorpay.com/v1/checkout.js";
            script.onload = () => resolve(true);
            script.onerror = () => resolve(false);
            document.body.appendChild(script);
        });

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

            const pendingResponse = await api.post(`/bookings/${bookingId}/payment/initiate`);
            setBooking(pendingResponse.data);

            const ok = await loadRazorpayScript();
            if (!ok) {
                throw new Error("Razorpay SDK failed to load");
            }

            const amountPaise = Math.round(totalPrice * 100);
            if (!amountPaise || amountPaise <= 0) {
                throw new Error("Invalid amount for payment");
            }

            const keyId = import.meta.env.VITE_RAZORPAY_KEY_ID;
            if (!keyId) {
                throw new Error("Missing Razorpay key");
            }

            await new Promise((resolve, reject) => {
                const options = {
                    key: keyId,
                    amount: amountPaise,
                    currency: "INR",
                    name: layout?.movieName || "CineTicket",
                    description: `Booking ${bookingId}`,
                    notes: {
                        bookingId: String(bookingId),
                        showId: String(showId),
                    },
                    handler: async () => {
                        try {
                            const confirmResponse = await api.post(
                                `/bookings/${bookingId}/payment/confirm`
                            );
                            setBooking(confirmResponse.data);
                            resolve(true);
                        } catch (confirmErr) {
                            reject(confirmErr);
                        }
                    },
                    modal: {
                        ondismiss: () => {
                            reject(new Error("Payment cancelled"));
                        },
                    },
                    theme: {
                        color: "#1d4ed8",
                    },
                };

                const razorpay = new window.Razorpay(options);
                razorpay.open();
            });

        } catch (err) {
            if (err?.response?.status === 401 || err?.response?.status === 403) {
                navigate(`/login?redirect=/shows/${showId}/layout`);
                return;
            }
            console.error("Booking failed:", err);
            setError(
                err?.message === "Payment cancelled"
                    ? "Payment cancelled."
                    : "Booking failed. Please try again."
            );
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
                        {layout?.movieName || "Movie"} - {layout?.theatreName || "Theatre"} -{" "}
                        {layout?.screenName || "Screen"} - Show {layout?.showId || showId}
                    </p>
                    <p className="subtle">
                        Auto-refreshing every {Math.round(REFRESH_INTERVAL_MS / 1000)}s
                        {lastRefreshAt ? ` - Updated ${lastRefreshAt.toLocaleTimeString()}` : ""}
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
                {priceNumber !== null && (
                    <span className="legend-item price">Base: ₹{priceNumber.toFixed(0)}</span>
                )}
            </div>

            <div className="seat-layout">
                <div className="seat-map">
                    <div className="seat-toolbar">
                        <div className="screen-label">Screen</div>
                        <div className="zoom-control">
                            <span>Zoom</span>
                            <input
                                type="range"
                                min="0.8"
                                max="1.4"
                                step="0.1"
                                value={zoom}
                                onChange={(e) => setZoom(Number(e.target.value))}
                            />
                        </div>
                    </div>
                    <div className="seat-grid" style={{ transform: `scale(${zoom})` }}>
                        {rows.map((row, rowIndex) => (
                            <div key={`row-${rowIndex}`} className="seat-row">
                                <div className="row-label">{getRowLabel(rowIndex)}</div>
                                <div className="row-seats">
                                    {row.map((seat) => {
                                        const priceForSeat = seatPriceForType(seat.seatType);
                                        return (
                                            <div
                                                key={seat.showSeatId}
                                                className={`seat ${String(seat.status || "").toLowerCase()} ${seat.status === "AVAILABLE" ? "selectable" : ""} ${selectedSeats.includes(seat.showSeatId) ? "selected" : ""}`}
                                                title={`Seat ${seat.seatNumber} - ${seat.seatType || "REGULAR"} - ${priceForSeat !== null ? `₹${priceForSeat.toFixed(0)}` : "Price TBD"}`}
                                                onClick={() => toggleSeat(seat)}
                                            >
                                                {seat.seatNumber}
                                            </div>
                                        );
                                    })}
                                </div>
                            </div>
                        ))}
                    </div>
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
                            {layout?.startTime ? new Date(layout.startTime).toLocaleString() : "TBD"}
                        </span>
                    </div>
                    <div className="summary-row">
                        <span>Seats</span>
                        <span>{selectedSeatNumbers.length ? selectedSeatNumbers.join(", ") : "None"}</span>
                    </div>
                    <div className="summary-row">
                        <span>Base price</span>
                        <span>{priceNumber !== null ? `₹${priceNumber.toFixed(2)}` : "TBD"}</span>
                    </div>
                    {seatTypePrices.length > 1 && (
                        <div className="summary-row">
                            <span>Seat types</span>
                            <span>
                                {seatTypePrices
                                    .map((item) =>
                                        item.price !== null
                                            ? `${item.type} ₹${item.price.toFixed(0)}`
                                            : item.type
                                    )
                                    .join(", ")}
                            </span>
                        </div>
                    )}
                    <div className="summary-row total">
                        <span>Total</span>
                        <span>{selectedSeats.length ? `₹${totalPrice.toFixed(2)}` : "TBD"}</span>
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
