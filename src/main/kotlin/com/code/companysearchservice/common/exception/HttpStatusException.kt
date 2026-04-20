package com.code.companysearchservice.common.exception

import org.springframework.http.HttpStatus
import java.time.Instant

class HttpStatusException(
    val status: HttpStatus,
    override val message: String,
    val timestamp: Instant = Instant.now(),
): RuntimeException(message)