# StaySphere — Frontend

React (Vite) frontend for the StaySphere Spring Boot backend.

## Setup

```bash
npm install
cp .env.example .env   # point VITE_API_BASE_URL at your running backend
npm run dev
```

Runs at `http://localhost:5173`. Make sure the Spring Boot backend (default `http://localhost:8080`)
and, if you're testing payments, the .NET Payment microservice are running and reachable, and that
CORS is enabled on the backend for `http://localhost:5173`.

## Backend CORS note

The Spring Boot `SecurityConfig` built earlier in this project doesn't have a CORS configuration yet —
add one (e.g. a `CorsConfigurationSource` bean allowing `http://localhost:5173`) or requests from this
frontend will be blocked by the browser. Say so if you'd like that added to the Java side.

## Routes

| Path | Who | Purpose |
|---|---|---|
| `/` | anyone | Search + browse listings |
| `/properties/:id` | anyone | Property details, request a booking |
| `/login`, `/register` | anyone | Auth |
| `/my-bookings` | TENANT | View/cancel bookings, pay, simulate payment result |
| `/owner/dashboard` | OWNER | Totals + per-property earnings/bookings |
| `/owner/properties` | OWNER | Create/edit/delete listings |
| `/owner/properties/:id/manage` | OWNER | Manage photos and facilities |
| `/owner/bookings` | OWNER | Confirm/reject booking requests |
| `/admin` | ADMIN | Manage users, properties, bookings |

## Notes on the payment flow

There's no real payment gateway integration. "Pay now" on `/my-bookings` calls
`POST /api/transactions`, which (per the backend) calls out to the .NET Payment microservice and
creates a `PENDING` transaction. The "Mark payment successful / failed" buttons that appear next
call `PUT /api/transactions/{id}/status` to simulate a gateway callback — replace that step with a
real gateway SDK when you're ready to go live.

## Design

Palette and type tokens are in `src/index.css`. The visual motif is a doorway: property cards get an
arch-shaped image slot with a brass "key tag" showing the rent, reused as the status-badge style
throughout (`StatusBadge` / `.tag`).
