# College Bus Tracking — Backend

Independent Spring Boot 3.x Maven application. Open this `backend/` folder in IntelliJ IDEA.

## Requirements

- Java 21 (Java 17+ can work if you change `java.version` in `pom.xml`)
- Maven 3.9+ or the included wrapper (`mvnw.cmd` on Windows)
- MySQL 8

## MySQL setup

```sql
CREATE DATABASE college_bus_tracking CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

## Environment variables

Do not hardcode secrets. Set these before running:

| Variable | Example | Required |
| --- | --- | --- |
| `DB_HOST` | `localhost` | No (default `localhost`) |
| `DB_PORT` | `3306` | No |
| `DB_NAME` | `college_bus_tracking` | No |
| `DB_USERNAME` | `root` | No |
| `DB_PASSWORD` | your password | Yes in real environments |
| `JWT_SECRET` | 32+ character secret | **Yes** |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173` | No |
| `SEED_DATA` | `true` | No |
| `SEED_ADMIN_EMAIL` | `admin@college.edu` | No |
| `SEED_ADMIN_PASSWORD` | strong password | No |

Windows PowerShell example:

```powershell
$env:JWT_SECRET = "change-this-to-a-long-random-secret-key-32chars"
$env:DB_PASSWORD = "your-mysql-password"
$env:DB_USERNAME = "root"
$env:DB_NAME = "college_bus_tracking"
```

## Run with Maven

```powershell
.\mvnw.cmd spring-boot:run
```

If system Maven is installed:

```powershell
mvn spring-boot:run
```

## Run with IntelliJ IDEA

1. Open `CollegeBusTrackingSystem/backend` as a Maven project.
2. Add environment variables `JWT_SECRET` and `DB_PASSWORD` to the run configuration.
3. Run `CollegeBusTrackingApplication`.

Backend: [http://localhost:8080](http://localhost:8080)

Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

OpenAPI: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

## Seed accounts (when `SEED_DATA=true`)

| Role | Email | Password |
| --- | --- | --- |
| ADMIN | `admin@college.edu` | `Admin@123` |
| DRIVER | `driver@college.edu` | `Driver@123` |
| STUDENT | `student@college.edu` | `Student@123` |

Admin cannot be created through public signup. Students use `POST /api/auth/signup`. The web login screen lets users choose Student, Driver, or Admin; the server checks that the selected role matches the authenticated account. Admins create driver accounts from the admin dashboard.

Both `src/main/resources/application.yml` and `application.properties` contain equivalent Spring settings. Keep duplicate values synchronized because Spring Boot gives `.properties` precedence when both files are present.

## REST API (selected)

Authentication

- `POST /api/auth/signup`
- `POST /api/auth/login`
- `POST /api/auth/refresh`

Admin

- `GET /api/admin/dashboard`
- `GET /api/admin/users`
- `GET /api/admin/drivers`
- `POST /api/admin/drivers`
- `GET /api/admin/students`
- `GET|POST /api/admin/buses`
- `PUT|DELETE /api/admin/buses/{id}`
- `GET /api/admin/routes`
- `GET /api/admin/trips`
- `GET /api/admin/buses/{busId}/history`

Driver

- `POST /api/driver/route`
- `PUT|DELETE /api/driver/route/{routeId}`
- `POST /api/driver/stop`
- `PUT /api/driver/stop/{stopId}`
- `DELETE /api/driver/stop/{stopId}`
- `POST /api/driver/trip/start`
- `POST /api/driver/trip/{tripId}/pause`
- `POST /api/driver/trip/{tripId}/resume`
- `POST /api/driver/trip/{tripId}/end`
- `POST /api/bus/{busId}/location`

Student / tracking

- `GET /api/buses`
- `GET /api/buses/{busId}/stops`
- `POST /api/student/select-stop`
- `GET /api/student/tracking/{busId}`
- `GET /api/stop/{stopId}/eta`

## WebSocket

- Endpoint: `/ws` (STOMP over SockJS, plus a raw STOMP endpoint)
- Topics:
  - `/topic/bus/{busId}/location`
  - `/topic/bus/{busId}/status`
  - `/topic/bus/{busId}/arrival`

Authorize REST calls with `Authorization: Bearer <accessToken>`.

## Tests

```powershell
.\mvnw.cmd clean test
```

Unit tests cover Haversine distance, moving-average speed, GPS jitter rejection, ETA fallback, and stop arrival.
