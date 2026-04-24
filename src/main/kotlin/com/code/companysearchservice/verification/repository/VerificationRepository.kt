package com.code.companysearchservice.verification.repository

import com.code.companysearchservice.verification.repository.model.VerificationEntity
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.ListCrudRepository
import org.springframework.data.repository.query.Param
import java.util.UUID

interface VerificationRepository : ListCrudRepository<VerificationEntity, UUID> {

    fun findByVerificationId(verificationId: UUID): VerificationEntity?

    @Query(
        """
        INSERT INTO verifications (id, verification_id, query_text, timestamp, result, source)
        VALUES (:#{#entity.id}, :#{#entity.verificationId}, :#{#entity.queryText}, :#{#entity.timestamp}, :#{#entity.result}, :#{#entity.source.name()})
        ON CONFLICT (verification_id) DO UPDATE SET verification_id = EXCLUDED.verification_id
        RETURNING *
        """
    )
    fun upsert(@Param("entity") entity: VerificationEntity): VerificationEntity
}
