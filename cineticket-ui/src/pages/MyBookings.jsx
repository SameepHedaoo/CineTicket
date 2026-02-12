import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { api } from "../api/api";
import QRCode from "qrcode";
import { jsPDF } from "jspdf";
import { notifySuccess } from "../ui/notificationBus";

function MyBookings() {
    const navigate = useNavigate();
    const [bookings, setBookings] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [qrCache, setQrCache] = useState({});
    const [downloadingId, setDownloadingId] = useState(null);

    const token = useMemo(() => localStorage.getItem("token"), []);

    useEffect(() => {
        if (!token) {
            return;
        }
        setLoading(true);
        setError(null);
        api.get("/bookings/me")
            .then((res) => setBookings(res.data || []))
            .catch((err) => {
                console.error("Failed to load bookings:", err);
                setError("Failed to load bookings.");
            })
            .finally(() => setLoading(false));
    }, [token]);

    const formatCurrency = (value) => {
        if (value === null || value === undefined || Number.isNaN(Number(value))) {
            return "TBD";
        }
        return `₹${Number(value).toFixed(2)}`;
    };

    const buildTicketPayload = (booking) => ({
        bookingId: booking.bookingId,
        showId: booking.showId,
        userId: booking.userId,
        seats: booking.seats,
        movieName: booking.movieName,
        theatreName: booking.theatreName,
        screenName: booking.screenName,
        showStartTime: booking.showStartTime,
    });

    const getQrForBooking = async (booking) => {
        const existing = qrCache[booking.bookingId];
        if (existing) {
            return existing;
        }
        const payload = JSON.stringify(buildTicketPayload(booking));
        const dataUrl = await QRCode.toDataURL(payload, { width: 320, margin: 1 });
        setQrCache((prev) => ({ ...prev, [booking.bookingId]: dataUrl }));
        return dataUrl;
    };

    const handleDownloadQr = async (booking) => {
        setDownloadingId(booking.bookingId);
        try {
            const qrDataUrl = await getQrForBooking(booking);
            const link = document.createElement("a");
            link.href = qrDataUrl;
            link.download = `ticket-${booking.bookingId}.png`;
            link.click();
            notifySuccess(`QR downloaded for booking #${booking.bookingId}.`);
        } finally {
            setDownloadingId(null);
        }
    };

    const handleDownloadPdf = async (booking) => {
        setDownloadingId(booking.bookingId);
        try {
            const qrDataUrl = await getQrForBooking(booking);
            const doc = new jsPDF({ unit: "pt", format: "a4" });
            doc.setFontSize(18);
            doc.text("CineTicket - Booking Ticket", 40, 50);
            doc.setFontSize(12);
            const details = [
                `Booking ID: ${booking.bookingId}`,
                `Movie: ${booking.movieName || "TBD"}`,
                `Theatre: ${booking.theatreName || "TBD"}`,
                `Screen: ${booking.screenName || "TBD"}`,
                `Showtime: ${booking.showStartTime ? new Date(booking.showStartTime).toLocaleString() : "TBD"}`,
                `Seats: ${booking.seats?.length ? booking.seats.join(", ") : "TBD"}`,
                `Total: ${formatCurrency(booking.totalPrice)}`,
                `Status: ${booking.status || "TBD"}`,
                `Payment: ${booking.paymentStatus || "TBD"}`,
            ];
            let y = 90;
            details.forEach((line) => {
                doc.text(line, 40, y);
                y += 20;
            });
            doc.text("Scan QR for verification", 40, y + 20);
            doc.addImage(qrDataUrl, "PNG", 40, y + 40, 160, 160);
            doc.save(`ticket-${booking.bookingId}.pdf`);
            notifySuccess(`Ticket PDF downloaded for booking #${booking.bookingId}.`);
        } finally {
            setDownloadingId(null);
        }
    };

    const isBookingExpired = (booking) =>
        String(booking?.status || "").toLowerCase() === "expired";

    if (!token) {
        return (
            <div className="page">
                <div className="card">
                    <div className="card-title">My Bookings</div>
                    <div className="card-meta">Please log in to view your bookings.</div>
                    <button className="primary" onClick={() => navigate("/login?redirect=/my-bookings")}>
                        Go to Login
                    </button>
                </div>
            </div>
        );
    }

    return (
        <div className="page">
            <div className="page-header">
                <div>
                    <h2>My Bookings</h2>
                    <p className="subtle">View your bookings and download tickets.</p>
                </div>
            </div>

            {error && <div className="error">{error}</div>}
            {loading && <div className="muted">Loading bookings...</div>}

            {!loading && bookings.length === 0 && !error && (
                <div className="muted">No bookings yet.</div>
            )}

            <div className="card-grid">
                {bookings.map((booking) => (
                    <div key={booking.bookingId} className="card booking-card">
                        <div className="card-title">{booking.movieName || "Movie TBD"}</div>
                        <div className="card-meta">
                            {booking.theatreName || "Theatre TBD"} • {booking.screenName || "Screen TBD"}
                        </div>
                        <div className="card-meta">
                            Showtime:{" "}
                            {booking.showStartTime
                                ? new Date(booking.showStartTime).toLocaleString()
                                : "TBD"}
                        </div>
                        <div className="card-meta">
                            Seats: {booking.seats?.length ? booking.seats.join(", ") : "TBD"}
                        </div>
                        <div className="card-meta">Total: {formatCurrency(booking.totalPrice)}</div>
                        <div className="booking-status-row">
                            <span className={`status-pill ${String(booking.status || "").toLowerCase()}`}>
                                {booking.status || "UNKNOWN"}
                            </span>
                            <span className={`status-pill ${String(booking.paymentStatus || "").toLowerCase()}`}>
                                {booking.paymentStatus || "PAYMENT"}
                            </span>
                        </div>
                        <div className="booking-actions">
                            <button
                                className="secondary"
                                type="button"
                                onClick={() => handleDownloadQr(booking)}
                                disabled={downloadingId === booking.bookingId || isBookingExpired(booking)}
                            >
                                {downloadingId === booking.bookingId ? "Preparing..." : "Download QR"}
                            </button>
                            <button
                                className="primary"
                                type="button"
                                onClick={() => handleDownloadPdf(booking)}
                                disabled={downloadingId === booking.bookingId || isBookingExpired(booking)}
                            >
                                {downloadingId === booking.bookingId ? "Preparing..." : "Download Ticket"}
                            </button>
                        </div>
                        {isBookingExpired(booking) && (
                            <div className="muted">Ticket expired (show time has passed).</div>
                        )}
                    </div>
                ))}
            </div>
        </div>
    );
}

export default MyBookings;
