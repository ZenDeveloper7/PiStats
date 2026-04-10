package com.zen.pistats.dashboard.domain

data class PiStats(
    val host: String,
    val uptimeSeconds: Long,
    val cpuPercent: Double,
    val memory: MemoryStats,
    val disk: DiskStats,
    val temperatureC: Double?,
    val loadAverage: List<Double>,
    val backupDrive: BackupDrive,
    val services: List<ServiceStatus>,
    val generatedAt: String,
)

data class MemoryStats(
    val usedMb: Int,
    val totalMb: Int,
)

data class DiskStats(
    val rootUsedGb: Double,
    val rootTotalGb: Double,
    val rootUsedPercent: Double,
)

data class BackupDrive(
    val connected: Boolean,
    val mounted: Boolean,
    val label: String?,
    val device: String?,
    val mountpoint: String?,
)

data class ServiceStatus(
    val name: String,
    val status: String,
    val detail: String,
)
