# PiStats

PiStats is the Android client for a private Raspberry Pi monitoring setup.

It is built with Kotlin and Jetpack Compose, polls a companion Pi backend API,
shows live Raspberry Pi telemetry, and can trigger a protected Wake-on-LAN relay
through the Pi.

The Pi backend now lives in a separate repository. Keep backend changes there and
use this repo for Android app, widget, and client-side documentation.

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
- Wake-on-LAN relay through the Pi for one configured PC

## Project layout

```text
app/                  Android app
docs/                 Android/backend contract notes
```

## Architecture

### Backend contract

- Companion backend repository owns the Pi service implementation
- This app expects bearer-token auth
- Backend should stay private behind Tailscale
- Android accepts only Tailscale IPs or MagicDNS `.ts.net` hosts

### Android app

- Kotlin
- Jetpack Compose
- Ktor client
- DataStore-backed settings
- polling-based dashboard
- Wake PC dashboard action
- resizable home-screen widget
- Tailscale-only endpoint validation

## Expected API endpoints

- `GET /api/health`
- `GET /api/stats`
- `POST /api/wakeonlan/wake`

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

### Run the Android app

```bash
./gradlew :app:installDebug
```

Then in the app:

1. Open Settings
2. Enter the Pi Tailscale URL
3. Enter the bearer token
4. Return to the dashboard

The Android app accepts only:

- Tailscale IPv4 addresses in `100.64.0.0/10`, for example `http://100.101.102.103:8787`
- MagicDNS hostnames ending in `.ts.net`

## Widget support

The app includes a home-screen widget that shows the same live stats contract in a large resizable layout.

Important limitation:

- the in-app dashboard refreshes every few seconds
- the home-screen widget cannot refresh that aggressively on Android
- the widget updates after settings changes, app refreshes, and periodic background work

## Backend deployment

Backend deployment is handled in the separate backend repository. This Android
client only needs the final Tailscale base URL and token.

For the client/backend contract, see:

- [Implementation Plan](docs/IMPLEMENTATION_PLAN.md)
- [Pi Deployment Guide](docs/PI_DEPLOYMENT.md)

## Status

Currently implemented:

- Android client for authenticated stats endpoints
- Android Wake PC action
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
