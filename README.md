# PiStats

PiStats is a practical Raspberry Pi monitoring project with two parts:

- a lightweight read-only Pi-side HTTP JSON API
- an Android app built with Kotlin and Jetpack Compose that polls that API

The project is optimized for a fast v1: low backend overhead, a stable JSON contract early, and an Android dashboard that can ship without overengineering.

## What it monitors in v1

- CPU usage
- RAM usage
- root filesystem disk usage
- uptime
- system temperature
- load average
- Docker container status for:
  - `vaultwarden`
  - `trilium`
  - `samba`
  - `pihole`
- backup drive state:
  - connected or not
  - mounted or not
  - label and device when available

## Project layout

```text
app/                  Android app
pi-backend/           Raspberry Pi stats API
docs/                 Planning and deployment docs
```

## Architecture

### Pi backend

- Python standard library HTTP server
- read-only JSON endpoints
- bearer-token auth
- bound to `127.0.0.1` by default
- intended to stay private behind Tailscale or another private network path

### Android app

- Kotlin
- Jetpack Compose
- Ktor client
- DataStore-backed settings
- polling-based dashboard
- resizable home-screen widget

## API endpoints

- `GET /api/health`
- `GET /api/stats`
- `GET /api/services`
- `GET /api/backup-status`

Example `GET /api/stats` response:

```json
{
  "host": "pi",
  "uptime_seconds": 123456,
  "cpu_percent": 18.4,
  "memory": {
    "used_mb": 512,
    "total_mb": 1900
  },
  "disk": {
    "root_used_gb": 51.0,
    "root_total_gb": 917.0,
    "root_used_percent": 6.0
  },
  "temperature_c": 48.2,
  "load_average": [0.21, 0.34, 0.40],
  "backup_drive": {
    "connected": true,
    "mounted": false,
    "label": "PiBackup",
    "device": "/dev/sdb2",
    "mountpoint": null
  },
  "services": [
    { "name": "vaultwarden", "status": "up", "detail": "running" },
    { "name": "trilium", "status": "up", "detail": "running" },
    { "name": "samba", "status": "up", "detail": "running" },
    { "name": "pihole", "status": "up", "detail": "running" }
  ],
  "generated_at": "2026-04-10T12:00:00Z"
}
```

## Quick start

### 1. Run the Pi backend locally

```bash
cd pi-backend
PISTATS_TOKEN=change-me python3 -m pi_backend.server
```

Test it:

```bash
curl -H "Authorization: Bearer change-me" http://127.0.0.1:8787/api/stats
```

### 2. Run the Android app

```bash
./gradlew :app:installDebug
```

Then in the app:

1. Open Settings
2. Enter the Pi API base URL
3. Enter the bearer token
4. Return to the dashboard

## Widget support

The app includes a home-screen widget that shows the same live stats contract in a large resizable layout.

Important limitation:

- the in-app dashboard refreshes every few seconds
- the home-screen widget cannot refresh that aggressively on Android
- the widget updates after settings changes, app refreshes, and periodic background work

## Pi deployment

For Raspberry Pi deployment details, see:

- [Backend README](pi-backend/README.md)
- [Implementation Plan](docs/IMPLEMENTATION_PLAN.md)
- [Pi Deployment Guide](docs/PI_DEPLOYMENT.md)
- [Example systemd service](pi-backend/pistats.service.example)

## Status

Currently implemented:

- Pi backend scaffold and collectors
- authenticated stats endpoints
- Android dashboard
- settings persistence
- polling-based refresh
- widget surface

## Verification

Verified locally:

- backend endpoints returned real JSON
- Android compile passed with:

```bash
./gradlew :app:compileDebugKotlin
```

## License

[MIT](LICENSE)
