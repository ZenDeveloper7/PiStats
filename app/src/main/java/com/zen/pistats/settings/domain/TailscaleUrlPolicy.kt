package com.zen.pistats.settings.domain

import java.net.URI

object TailscaleUrlPolicy {
    fun isAllowed(baseUrl: String): Boolean {
        val uri = runCatching { URI(baseUrl) }.getOrNull() ?: return false
        val scheme = uri.scheme?.lowercase() ?: return false
        if (scheme != "http" && scheme != "https") return false

        val host = uri.host?.lowercase() ?: return false
        if (host.endsWith(".ts.net")) return true

        return isTailscaleIp(host)
    }

    private fun isTailscaleIp(host: String): Boolean {
        val parts = host.split(".")
        if (parts.size != 4) return false
        val octets = parts.map { it.toIntOrNull() ?: return false }
        val first = octets[0]
        val second = octets[1]

        if (first != 100) return false
        return second in 64..127
    }
}
