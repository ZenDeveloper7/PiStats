# PiStats Backend

Small private JSON API for Raspberry Pi monitoring plus a protected Wake-on-LAN relay.

## Features

- Bearer-token auth
- Localhost binding by default
- Optional direct binding to the Pi Tailscale interface
- Read-only monitoring endpoints
- Protected Wake-on-LAN write endpoint
- Lightweight Linux collectors
- Docker-aware service status
- Backup drive detection
- Wake-on-LAN relay over the Pi's LAN

## Run locally

```bash
PISTATS_TOKEN=change-me python3 -m pi_backend.server
```

The server binds to `127.0.0.1:8787` by default.

To expose it on the Pi's Tailscale IP instead:

```bash
PISTATS_TOKEN=change-me \
PISTATS_BIND_MODE=tailscale \
PISTATS_WAKE_MAC=34:5a:60:f9:4b:96 \
python3 -m pi_backend.server
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
- `PISTATS_WAKE_MAC`
  - PC MAC address to wake, for example `34:5a:60:f9:4b:96`.
- `PISTATS_WAKE_BROADCAST`
  - LAN broadcast address used for the magic packet.
  - Default: `192.168.1.255`
- `PISTATS_WAKE_PORT`
  - UDP port used for Wake-on-LAN.
  - Default: `9`

## Endpoints

All endpoints require auth unless `PISTATS_DEV_MODE=1`.

```bash
curl -H "Authorization: Bearer change-me" http://127.0.0.1:8787/api/health
curl -H "Authorization: Bearer change-me" http://127.0.0.1:8787/api/stats
curl -X POST -H "Authorization: Bearer change-me" http://127.0.0.1:8787/api/wakeonlan/wake
curl -X POST -H "X-Wake-Token: change-me" http://127.0.0.1:8787/api/wakeonlan/wake
```

### `GET /api/health`

Returns backend health plus feature availability:

```json
{
  "status": "ok",
  "features": {
    "stats": true,
    "wakeonlan": true
  }
}
```

### `POST /api/wakeonlan/wake`

The Wake-on-LAN endpoint sends a magic packet from the Pi to the configured LAN
broadcast address. It accepts the same bearer-token auth as the stats API, or
`X-Wake-Token` with the same token for simple curl/app clients, and should stay
reachable only through localhost, LAN, or Tailscale.

Success response:

```json
{
  "status": "sent",
  "broadcast": "192.168.1.255",
  "port": 9
}
```

If `PISTATS_WAKE_MAC` is missing or invalid, the endpoint returns `400` with a
small JSON error body.

## Raspberry Pi deployment notes

- Leave the service in `localhost` mode unless you want Android access over Tailscale.
- For direct Android access over Tailscale, set `PISTATS_BIND_MODE=tailscale`.
- In `tailscale` mode, the backend binds to the Pi's `tailscale0` IPv4 address.
- The installer writes a `systemd` unit and starts `pistats.service`.
- Wake-on-LAN is sent from the Pi onto its local network, so the target PC must
  already have Wake-on-LAN enabled in firmware/OS and be reachable by LAN broadcast.

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
sudo ./install-on-pi.sh --wake-mac '34:5a:60:f9:4b:96' --wake-broadcast 192.168.1.255 --token 'replace-with-a-strong-token'
sudo ./install-on-pi.sh --force-env --token 'replace-with-a-strong-token'
```

Port behavior:

- `--port` is treated as the preferred starting port
- if that port is already occupied, the installer automatically checks the next ports upward until it finds a free one
- the selected port is written to `.env` and printed at the end
