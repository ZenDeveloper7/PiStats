package com.zen.pistats.dashboard.data

import com.zen.pistats.dashboard.domain.BackupDrive
import com.zen.pistats.dashboard.domain.DiskStats
import com.zen.pistats.dashboard.domain.MemoryStats
import com.zen.pistats.dashboard.domain.PiStats
import com.zen.pistats.dashboard.domain.ServiceStatus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PiStatsDto(
    val host: String,
    @SerialName("uptime_seconds") val uptimeSeconds: Long,
    @SerialName("cpu_percent") val cpuPercent: Double,
    val memory: MemoryDto,
    val disk: DiskDto,
    @SerialName("temperature_c") val temperatureC: Double? = null,
    @SerialName("load_average") val loadAverage: List<Double>,
    @SerialName("backup_drive") val backupDrive: BackupDriveDto,
    val services: List<ServiceStatusDto>,
    @SerialName("generated_at") val generatedAt: String,
)

@Serializable
data class MemoryDto(
    @SerialName("used_mb") val usedMb: Int,
    @SerialName("total_mb") val totalMb: Int,
)

@Serializable
data class DiskDto(
    @SerialName("root_used_gb") val rootUsedGb: Double,
    @SerialName("root_total_gb") val rootTotalGb: Double,
    @SerialName("root_used_percent") val rootUsedPercent: Double,
)

@Serializable
data class BackupDriveDto(
    val connected: Boolean,
    val mounted: Boolean,
    val label: String? = null,
    val device: String? = null,
    val mountpoint: String? = null,
)

@Serializable
data class ServiceStatusDto(
    val name: String,
    val status: String,
    val detail: String,
)

fun PiStatsDto.toPiStats(): PiStats {
    return PiStats(
        host = host,
        uptimeSeconds = uptimeSeconds,
        cpuPercent = cpuPercent,
        memory = MemoryStats(
            usedMb = memory.usedMb,
            totalMb = memory.totalMb,
        ),
        disk = DiskStats(
            rootUsedGb = disk.rootUsedGb,
            rootTotalGb = disk.rootTotalGb,
            rootUsedPercent = disk.rootUsedPercent,
        ),
        temperatureC = temperatureC,
        loadAverage = loadAverage,
        backupDrive = BackupDrive(
            connected = backupDrive.connected,
            mounted = backupDrive.mounted,
            label = backupDrive.label,
            device = backupDrive.device,
            mountpoint = backupDrive.mountpoint,
        ),
        services = services.map { service ->
            ServiceStatus(
                name = service.name,
                status = service.status,
                detail = service.detail,
            )
        },
        generatedAt = generatedAt,
    )
}
