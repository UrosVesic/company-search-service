package com.code.companysearchservice.verification.controller.model

import com.code.companysearchservice.backend.controller.model.ResultDTO
import com.code.companysearchservice.backend.service.model.CompanySource
import java.time.Instant
import java.util.UUID

data class VerificationDTO(
    val verificationId: UUID,
    val queryText: String,
    val timestamp: Instant,
    val result: ResultDTO,
    val source: CompanySource
)
