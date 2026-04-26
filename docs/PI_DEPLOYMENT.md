# Backend Connection Guide

## Goal

Configure the Android app to talk to the separate PiStats backend repository over
Tailscale.

Backend code, Pi install scripts, systemd units, and Raspberry Pi deployment
steps live in the backend repo. This Android repo only documents the client-side
contract it expects.

## Required Backend Contract

The backend must expose these authenticated endpoints:

```text
GET  /api/health
GET  /api/stats
POST /api/wakeonlan/wake
```

The Android app sends:

```text
Authorization: Bearer <token>
```

For Wake-on-LAN it also sends:

```text
X-Wake-Token: <token>
```

## Android App Configuration

In the app Settings screen, enter:

- Base URL:
  - a Tailscale Pi address, for example `http://100.x.y.z:8787`
  - or a MagicDNS hostname ending in `.ts.net`
- Auth token:
  - the token configured in the backend repo

The app rejects non-Tailscale base URLs. Keep the backend reachable only through
Tailscale or another private network path.

## Wake-on-LAN

The Dashboard Wake PC button calls:

```text
POST <base-url>/api/wakeonlan/wake
```

The PC MAC address, broadcast address, and UDP Wake-on-LAN port are backend
configuration. The Android app does not store or send the PC MAC address.

## Smoke Tests

From any device on your tailnet:

```bash
curl -H "Authorization: Bearer <token>" http://100.x.y.z:8787/api/health
curl -H "Authorization: Bearer <token>" http://100.x.y.z:8787/api/stats
curl -X POST -H "X-Wake-Token: <token>" http://100.x.y.z:8787/api/wakeonlan/wake
```

Once these pass, use the same base URL and token in the Android app.

## Security Notes

- Keep the backend off public port forwarding.
- Use a long random token.
- Prefer binding the backend to the Pi Tailscale IP.
- Confirm the PC can wake from the target power state before debugging Android.
