# PiStats Backend

Small read-only JSON API for Raspberry Pi monitoring.

## Features

- Bearer-token auth
- Localhost binding by default
- Optional direct binding to the Pi Tailscale interface
- No write endpoints
- Lightweight Linux collectors
- Docker-aware service status
- Backup drive detection

## Run locally

```bash
PISTATS_TOKEN=change-me python3 -m pi_backend.server
```

The server binds to `127.0.0.1:8787` by default.

To expose it on the Pi's Tailscale IP instead:

```bash
PISTATS_TOKEN=change-me PISTATS_BIND_MODE=tailscale python3 -m pi_backend.server
```

## Environment variables

- `PISTATS_TOKEN`
  - Required for non-dev usage.
- `PISTATS_BIND_MODE`
  - One of: `localhost`, `tailscale`, `custom`
  - Default: `localhost`
- `PISTATS_HOST`
  - Used for `custom` mode, or as an override for `localhost`
  - Default: `127.0.0.1`
- `PISTATS_TAILSCALE_IP`
  - Optional explicit Tailscale IP override
  - Useful if you want `tailscale` mode without auto-detecting `tailscale0`
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

- Leave the service in `localhost` mode unless you want Android access over Tailscale.
- For direct Android access over Tailscale, set `PISTATS_BIND_MODE=tailscale`.
- In `tailscale` mode, the backend binds to the Pi's `tailscale0` IPv4 address.
- A small `systemd` unit can be added once the API shape is confirmed on the Pi.

## Install script

The backend directory includes a repeatable Pi installer:

```bash
cd pi-backend
sudo ./install-on-pi.sh --token 'replace-with-a-strong-token'
```

Useful variations:

```bash
sudo ./install-on-pi.sh --port 8788 --token 'replace-with-a-strong-token'
sudo ./install-on-pi.sh --bind-mode localhost --token 'replace-with-a-strong-token'
sudo ./install-on-pi.sh --force-env --token 'replace-with-a-strong-token'
```

Port behavior:

- `--port` is treated as the preferred starting port
- if that port is already occupied, the installer automatically checks the next ports upward until it finds a free one
- the selected port is written to `.env` and printed at the end
