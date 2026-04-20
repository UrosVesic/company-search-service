package com.code.companysearchservice.verification.mapper

import com.code.companysearchservice.backend.controller.model.ResultDTO
import com.code.companysearchservice.verification.controller.model.VerificationDTO
import com.code.companysearchservice.verification.repository.model.VerificationEntity
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.stereotype.Component

@Component
class VerificationMapper(val objectMapper: ObjectMapper) {
    fun toVerificationDTO(verificationEntity: VerificationEntity) =
        with(verificationEntity) {
            VerificationDTO(
                verificationId = verificationId,
                queryText = queryText,
                timestamp = timestamp,
                result = objectMapper.readValue<ResultDTO>(result),
                source = source
            )
        }
}
