# CineTicket - Movie Ticket Booking System

CineTicket is a movie ticket booking system built with Spring Boot and Oracle DB, with a React (Vite) client.

This repository is a modular Spring Boot application (modules/packages for auth, movies, theatres, shows, booking, payments), not independently deployed microservices.

## Tech Stack

Backend:
- Java 17+
- Spring Boot 3
- Spring Security (JWT)
- Spring Data JPA (Hibernate)
- Flyway migrations
- Oracle Database

Frontend:
- React
- Vite
- Axios

## Domain Model (High Level)

Core entities:
- `City`: centrally managed city list.
- `Theatre` -> `Screen` -> `Seat`: theatre structure and seating layout.
- `Movie`: movie metadata (title, language, duration, genre, rating, poster URL).
- `Show`: movie + screen + start/end time + base price.
- `ShowSeat`: per-show seat state (`AVAILABLE`, `LOCKED`, `BOOKED`).
- `Booking`: user booking associated with a `Show` and a set of `ShowSeat`s.

Booking status lifecycle:
- `IN_PROGRESS` during seat lock/payment initiation
- `CONFIRMED` after successful payment and seat booking
- `CANCELLED` when lock expires or user cancels
- `EXPIRED` after the show time has passed (ticket no longer valid)

## Security Model

JWT authentication:
- API requests use `Authorization: Bearer <token>`.
- Roles are represented as `ROLE_<ROLE>` authorities.

Roles:
- `USER`: browse and book tickets.
- `THEATRE_MANAGER`: manage movies (create/delete), upload/update posters, manage screens and shows for their theatre.
- `ADMIN`: full admin console + assign theatre managers.

Notes:
- `/bookings/**` and `/payments/**` require authentication.
- Public GET endpoints include `/movies/**`, `/theatres/**`, `/shows/**`, `/cities/**`, `/uploads/**`.

## Poster Storage

Poster upload supports two storage modes:

1. Local (default):
- Posters are written under `APP_UPLOAD_DIR/posters`.
- Publicly served via `GET /uploads/**`.
- URLs are stored as `/uploads/posters/<uuid>.<ext>`.

2. GitHub (optional):
- Uploads poster bytes to GitHub via the Contents API and returns a raw URL.
- If GitHub is selected but credentials are not configured, the backend falls back to local storage.

Config keys:
- `APP_POSTER_STORAGE=local|github`
- `APP_UPLOAD_DIR=uploads`
- `GITHUB_TOKEN`, `GITHUB_REPO_OWNER`, `GITHUB_REPO_NAME`, `GITHUB_REPO_BRANCH`, `GITHUB_REPO_FOLDER`

## Scheduling / Background Jobs

Scheduling is enabled via `@EnableScheduling`.

Background tasks:
- Seat lock expiry: periodically finds `IN_PROGRESS` bookings past `lockExpiryTime`, releases seats, and cancels the booking.
- Ticket expiry: periodically marks `CONFIRMED` + `PAID` bookings as `EXPIRED` after the show end time passes.

Additionally, booking queries update status in-line so clients see up-to-date expiry without waiting for the scheduler tick.

## Key API Endpoints (Overview)

Auth:
- `POST /auth/register`
- `POST /auth/login`

Cities:
- `GET /cities`

Movies:
- `GET /movies` (distinct movies for browsing)
- `GET /movies/all` (public DTO list)
- `POST /movies/upload-poster` (multipart; authenticated)
- `POST /movies/{movieId}/poster` (multipart; authenticated)
- `DELETE /movies/{movieId}` (authenticated)

Theatres:
- `GET /theatres?city=<name>`
- `GET /theatres/{id}`
- `POST /theatres/add` (admin)
- `POST /theatres/screens` (admin/manager)
- `DELETE /theatres/screens/{screenId}` (admin/manager)

Shows:
- `GET /shows?city=<name>`
- `GET /shows/theatre/{theatreId}`
- `GET /shows/by-movie?movieId=<id>`
- `GET /shows/{showId}/layout`
- `POST /admin/shows/add` (admin/manager)

Bookings:
- `POST /bookings` (lock seats and create booking)
- `POST /bookings/{bookingId}/payment/initiate`
- `POST /bookings/{bookingId}/payment/confirm`
- `POST /bookings/{bookingId}/confirm`
- `POST /bookings/{bookingId}/cancel`
- `GET /bookings/me`

## Database Migrations (Flyway)

Migrations live in `src/main/resources/db/migration`.

Examples:
- Initial schema: `V1__initial_schema.sql`
- Movie poster URL: `V2__add_movie_poster_url.sql`
- Booking enum constraint update (adds `EXPIRED`): `V3__booking_status_add_expired.sql`

If you already have an existing Oracle schema, run the app once to apply migrations (or run Flyway via the app).

## Local Development

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

Environment variables:
- Backend: `.env.example`
- Frontend: `cineticket-ui/.env.example`

## Testing

Backend tests:
```bash
./gradlew test
```

Frontend build:
```bash
cd cineticket-ui
npm run build
```

## Troubleshooting

ORA-02290 (check constraint violated) on booking status:
- Your DB likely has an old CHECK constraint for the booking status enum.
- Ensure Flyway migration `V3__booking_status_add_expired.sql` has run.

403/401 on bookings:
- `/bookings/**` requires a valid JWT.
- Ensure the client is sending `Authorization: Bearer <token>`.
