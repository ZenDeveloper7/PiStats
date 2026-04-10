# Pi Deployment Guide

## Goal

Run the PiStats backend as a private read-only monitoring API on your Raspberry Pi and point the Android app at it.

## Assumptions

- Raspberry Pi user: `zen`
- Docker services include:
  - `vaultwarden`
  - `trilium`
  - `samba`
  - `pihole`
- You want the API kept private
- Remote access, if any, goes through Tailscale or another private path

## Copy the backend to the Pi

From your development machine:

```bash
rsync -av pi-backend/ zen@pi:/home/zen/pistats-backend/
```

## Configure environment

Create an env file on the Pi:

```bash
cat >/home/zen/pistats-backend/.env <<'EOF'
PISTATS_TOKEN=replace-with-a-strong-token
PISTATS_HOST=127.0.0.1
PISTATS_PORT=8787
PISTATS_SERVICES=vaultwarden,trilium,samba,pihole
PISTATS_BACKUP_LABEL=PiBackup
# Optional:
# PISTATS_BACKUP_MOUNTPOINT=/media/zen/PiBackup
EOF
```

## Test manually on the Pi

```bash
cd /home/zen/pistats-backend
set -a
source .env
set +a
python3 -m pi_backend.server
```

In another shell on the Pi:

```bash
curl -H "Authorization: Bearer $PISTATS_TOKEN" http://127.0.0.1:8787/api/stats
```

## Install as a systemd service

Copy the included example service:

```bash
sudo cp /home/zen/pistats-backend/pistats.service.example /etc/systemd/system/pistats.service
sudo systemctl daemon-reload
sudo systemctl enable --now pistats.service
```

Then check:

```bash
sudo systemctl status pistats.service
journalctl -u pistats.service -n 100 --no-pager
```

## Android app configuration

In the app Settings screen, enter:

- base URL:
  - `http://127.0.0.1:8787` if using a local/private forwarding path
  - or your Tailscale/private Pi address, for example `http://100.x.y.z:8787`
- auth token:
  - the exact `PISTATS_TOKEN` value from the Pi

## Security notes

- Leave `PISTATS_HOST=127.0.0.1` unless you intentionally expose the service on a private network.
- Do not expose this API publicly on the internet for v1.
- Keep the token strong and unique.
- v1 has no write endpoints by design.
