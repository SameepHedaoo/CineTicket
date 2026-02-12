# CineTicket - Movie Ticket Booking Web App

CineTicket is a minimalist movie ticket booking app built with:
- Backend: Spring Boot + Spring Security + JPA + Flyway + Oracle DB
- Frontend: React (Vite) + Axios

The UX focuses on a clean flow from movie discovery to seat selection and booking.

## Features

### Browsing and Discovery
- Central city list (`/cities`) used across the UI.
- Movies landing page shows 1 card per movie for the selected city, with lightweight metadata.
- Shows page avoids redundancy:
  - All-shows mode: 1 card per movie overview (theatre count, show count, min price, next showtime)
  - Movie-selected mode: detailed show cards focused on theatre + price comparison

### Movie Detail Experience
- Movie detail page: poster, big title, structured metadata (genre/duration/language/rating), synopsis
- Show timings grouped by theatre, with quick "Select Seats" actions

### Seat Booking and Payments
- Seat layout with clear states (available/selected/locked/booked) and a legend
- Seat locking to avoid double booking
- Razorpay checkout integration (when enabled)

### Ticket Expiry
- Tickets automatically expire after show time (booking status becomes `EXPIRED`).
- Expired tickets are shown in "My Bookings" and ticket download is disabled.

### Role-Based Dashboard
- Single Login entrypoint
- `/dashboard` auto-routes based on JWT role:
  - `ADMIN` -> Admin console
  - `THEATRE_MANAGER` -> Manager console

## UI Routes (Frontend)
- `/movies` : movies landing (city filter)
- `/movies/:movieId` : movie detail (theatre-grouped showtimes)
- `/shows` : shows browsing (city filter + overview)
- `/shows/:showId/layout` : seat selection + booking
- `/my-bookings` : user bookings + ticket downloads (disabled for expired)
- `/dashboard` : role-based admin/manager console

## Poster Uploads (Local Default)

Posters are stored locally by default and served from:
- `GET /uploads/**`

The backend returns poster URLs like:
- `/uploads/posters/<uuid>.<ext>`

Optional GitHub storage can still be configured via env vars, but the backend will fall back to local storage if GitHub is not configured.

## Configuration

Backend env vars are documented in `.env.example`.
Frontend env vars are documented in `cineticket-ui/.env.example`.

Key backend vars:
- `DB_USERNAME`, `DB_PASSWORD`
- `JWT_SECRET`, `JWT_EXPIRATION`
- `ADMIN_EMAIL`, `ADMIN_PASSWORD`
- `APP_UPLOAD_DIR` (default `uploads`)
- `APP_POSTER_STORAGE` (default `local`)
- `RAZORPAY_ENABLED` (default `false`)

Key frontend vars:
- `VITE_API_BASE_URL` (default `http://localhost:8080`)
- `VITE_RAZORPAY_KEY_ID` (required only when Razorpay is enabled)

## Running Locally

Backend:
```bash
./gradlew bootRun
```

Frontend:
```bash
cd cineticket-ui
npm install
npm run dev
```

## Notes
- Flyway migrations live in `src/main/resources/db/migration`.
- If you already have an existing DB, ensure migrations run (booking status now includes `EXPIRED`).
