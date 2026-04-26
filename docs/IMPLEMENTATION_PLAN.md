# PiStats v1 Implementation Plan

## Repository Boundary

This repository contains the Android app only. The Pi backend lives in a separate
repository and owns implementation, Raspberry Pi install scripts, and systemd
deployment.

## Execution order

1. Define the stable JSON contract expected by the Android app.
2. Implement backend changes in the backend repository.
3. Smoke-test the backend over Tailscale with curl.
4. Build the Android client against that contract.
5. Add token auth, persisted settings, polling, and Wake PC UI.

## Backend scope

- Folder: separate backend repository
- Auth: `Authorization: Bearer <token>`
- Wake auth alias: `X-Wake-Token: <token>`
- Endpoints:
  - `GET /api/health`
  - `GET /api/stats`
  - `POST /api/wakeonlan/wake`

## Backend contract notes

- The Android app treats `baseUrl` as the backend root and appends `/api/...`.
- The app only accepts Tailscale IPs in `100.64.0.0/10` or `.ts.net` MagicDNS hosts.
- The backend response for `/api/stats` includes service and backup data, so the
  Android app does not call separate service or backup endpoints.
- Wake-on-LAN configuration stays on the backend; the Android app never sends the
  target PC MAC address.

## Android scope

- Single `:app` module for quick delivery
- Kotlin + Jetpack Compose
- Ktor client + Kotlinx Serialization
- DataStore for `baseUrl` and `authToken`
- Koin for DI
- Navigation with two screens:
  - Dashboard
  - Settings
- Polling every 5 seconds while dashboard is visible/configured
- Dashboard includes a Wake PC action that posts to `/api/wakeonlan/wake`

## Stable response contract

`GET /api/health`

```json
{
  "status": "ok",
  "features": {
    "stats": true,
    "wakeonlan": true
  }
}
```

`GET /api/stats`

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
  "load_average": [0.21, 0.34, 0.4],
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

`POST /api/wakeonlan/wake`

```json
{
  "status": "sent",
  "broadcast": "192.168.1.255",
  "port": 9
}
```

## Backend deployment outline

1. Make backend changes in the separate backend repository.
2. Deploy that backend to the Raspberry Pi.
3. Keep it private behind Tailscale.
4. Smoke-test `/api/health`, `/api/stats`, and `/api/wakeonlan/wake`.
5. Configure this Android app with the backend Tailscale base URL and token.
