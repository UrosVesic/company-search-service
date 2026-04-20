package com.code.companysearchservice.common.exception

import java.time.Instant

data class ErrorResponse(
    val message: String,
    val timestamp: Instant = Instant.now(),
    val status: Int
)
