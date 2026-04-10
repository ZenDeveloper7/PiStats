from __future__ import annotations

import os
from dataclasses import dataclass


DEFAULT_SERVICES = ("vaultwarden", "trilium", "samba", "pihole")


@dataclass(frozen=True)
class Settings:
    host: str
    port: int
    token: str
    dev_mode: bool
    services: tuple[str, ...]
    backup_label: str | None
    backup_mountpoint: str | None


def load_settings() -> Settings:
    services = tuple(
        value.strip()
        for value in os.getenv("PISTATS_SERVICES", ",".join(DEFAULT_SERVICES)).split(",")
        if value.strip()
    )
    return Settings(
        host=os.getenv("PISTATS_HOST", "127.0.0.1"),
        port=int(os.getenv("PISTATS_PORT", "8787")),
        token=os.getenv("PISTATS_TOKEN", ""),
        dev_mode=os.getenv("PISTATS_DEV_MODE", "0") == "1",
        services=services or DEFAULT_SERVICES,
        backup_label=_clean_env("PISTATS_BACKUP_LABEL"),
        backup_mountpoint=_clean_env("PISTATS_BACKUP_MOUNTPOINT"),
    )


def _clean_env(key: str) -> str | None:
    value = os.getenv(key, "").strip()
    return value or None
