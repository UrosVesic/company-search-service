package com.code.companysearchservice.verification.repository.model

import com.code.companysearchservice.backend.service.model.CompanySource
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.UUID

@Table("verifications")
data class VerificationEntity(
    @Id
    val id: UUID? = null,
    val verificationId: UUID,
    val queryText: String,
    val timestamp: Instant,
    val result: String,
    val source: CompanySource
)
