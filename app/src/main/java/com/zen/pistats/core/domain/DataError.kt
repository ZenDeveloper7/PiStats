package com.zen.pistats.core.domain

sealed interface DataError : AppError {
    enum class Network : DataError {
        UNAUTHORIZED,
        FORBIDDEN,
        NOT_FOUND,
        REQUEST_TIMEOUT,
        NO_INTERNET,
        SERVER_ERROR,
        SERIALIZATION,
        UNKNOWN,
    }
}
