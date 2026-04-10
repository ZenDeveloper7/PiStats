# PiStats Backend

Small read-only JSON API for Raspberry Pi monitoring.

## Features

- Bearer-token auth
- Localhost binding by default
- No write endpoints
- Lightweight Linux collectors
- Docker-aware service status
- Backup drive detection

## Run locally

```bash
PISTATS_TOKEN=change-me python3 -m pi_backend.server
```

The server binds to `127.0.0.1:8787` by default.

## Environment variables

- `PISTATS_TOKEN`
  - Required for non-dev usage.
- `PISTATS_HOST`
  - Default: `127.0.0.1`
- `PISTATS_PORT`
  - Default: `8787`
- `PISTATS_SERVICES`
  - Comma-separated Docker container names.
  - Default: `vaultwarden,trilium,samba,pihole`
- `PISTATS_BACKUP_LABEL`
  - Optional preferred filesystem label to search for.
- `PISTATS_BACKUP_MOUNTPOINT`
  - Optional expected mountpoint to check first.
- `PISTATS_DEV_MODE`
  - Set to `1` to disable auth for local development only.

## Endpoints

```bash
curl -H "Authorization: Bearer change-me" http://127.0.0.1:8787/api/health
curl -H "Authorization: Bearer change-me" http://127.0.0.1:8787/api/stats
curl -H "Authorization: Bearer change-me" http://127.0.0.1:8787/api/services
curl -H "Authorization: Bearer change-me" http://127.0.0.1:8787/api/backup-status
```

## Raspberry Pi deployment notes

- Leave the service bound to `127.0.0.1` unless you intentionally expose it on a private network.
- If you want Android access, prefer Tailscale or another private tunnel/path.
- A small `systemd` unit can be added once the API shape is confirmed on the Pi.
