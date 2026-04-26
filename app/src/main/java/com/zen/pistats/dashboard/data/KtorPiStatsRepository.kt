package com.zen.pistats.dashboard.data

import com.zen.pistats.core.domain.DataError
import com.zen.pistats.core.domain.Result
import com.zen.pistats.dashboard.domain.PiStats
import com.zen.pistats.settings.domain.AppSettings
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.SerializationException
import java.io.IOException
import java.net.SocketTimeoutException

class KtorPiStatsRepository(
    private val httpClient: HttpClient,
) : PiStatsRepository {
    override suspend fun fetchStats(settings: AppSettings): Result<PiStats, DataError.Network> {
        return try {
            val response = httpClient.get("${settings.baseUrl}/api/stats") {
                contentType(ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer ${settings.authToken}")
            }

            when (response.status) {
                HttpStatusCode.OK -> Result.Success(response.body<PiStatsDto>().toPiStats())
                HttpStatusCode.Unauthorized -> Result.Error(DataError.Network.UNAUTHORIZED)
                HttpStatusCode.Forbidden -> Result.Error(DataError.Network.FORBIDDEN)
                HttpStatusCode.NotFound -> Result.Error(DataError.Network.NOT_FOUND)
                HttpStatusCode.RequestTimeout -> Result.Error(DataError.Network.REQUEST_TIMEOUT)
                else -> {
                    if (response.status.value >= 500) {
                        Result.Error(DataError.Network.SERVER_ERROR)
                    } else {
                        Result.Error(DataError.Network.UNKNOWN)
                    }
                }
            }
        } catch (_: SocketTimeoutException) {
            Result.Error(DataError.Network.REQUEST_TIMEOUT)
        } catch (_: SerializationException) {
            Result.Error(DataError.Network.SERIALIZATION)
        } catch (_: IOException) {
            Result.Error(DataError.Network.NO_INTERNET)
        } catch (_: Exception) {
            Result.Error(DataError.Network.UNKNOWN)
        }
    }

    override suspend fun wakePc(settings: AppSettings): Result<Unit, DataError.Network> {
        return try {
            val response = httpClient.post("${settings.baseUrl}/api/wakeonlan/wake") {
                contentType(ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer ${settings.authToken}")
                header("X-Wake-Token", settings.authToken)
            }

            when (val error = response.toNetworkErrorOrNull()) {
                null -> Result.Success(Unit)
                else -> Result.Error(error)
            }
        } catch (_: SocketTimeoutException) {
            Result.Error(DataError.Network.REQUEST_TIMEOUT)
        } catch (_: IOException) {
            Result.Error(DataError.Network.NO_INTERNET)
        } catch (_: Exception) {
            Result.Error(DataError.Network.UNKNOWN)
        }
    }

    private fun HttpResponse.toNetworkErrorOrNull(): DataError.Network? {
        return when (status) {
            HttpStatusCode.OK -> null
            HttpStatusCode.Unauthorized -> DataError.Network.UNAUTHORIZED
            HttpStatusCode.Forbidden -> DataError.Network.FORBIDDEN
            HttpStatusCode.NotFound -> DataError.Network.NOT_FOUND
            HttpStatusCode.RequestTimeout -> DataError.Network.REQUEST_TIMEOUT
            else -> {
                if (status.value >= 500) {
                    DataError.Network.SERVER_ERROR
                } else {
                    DataError.Network.UNKNOWN
                }
            }
        }
    }
}
