package com.code.companysearchservice.verification.repository

import com.code.companysearchservice.verification.repository.model.VerificationEntity
import org.springframework.data.repository.ListCrudRepository
import java.util.UUID

interface VerificationRepository : ListCrudRepository<VerificationEntity, UUID> {

    fun findByVerificationId(verificationId: UUID): VerificationEntity?
}
