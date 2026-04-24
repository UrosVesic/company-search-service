package com.code.companysearchservice.verification.controller

import com.code.companysearchservice.BaseDatabaseIntegrationTest
import com.code.companysearchservice.backend.controller.model.ResultDTO
import com.code.companysearchservice.backend.service.model.CompanySource
import com.code.companysearchservice.backend.service.model.ServiceStatus
import com.code.companysearchservice.common.exception.ErrorResponse
import com.code.companysearchservice.verification.controller.model.VerificationDTO
import com.code.companysearchservice.verification.repository.VerificationRepository
import com.code.companysearchservice.verification.repository.model.VerificationEntity
import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.getForEntity
import org.springframework.http.HttpStatus
import java.time.OffsetDateTime
import java.util.UUID

class VerificationControllerIT(
    @Autowired val verificationRepository: VerificationRepository,
    @Autowired val objectMapper: ObjectMapper
) : BaseDatabaseIntegrationTest() {

    @Test
    fun `should return verification after it was created via backend service`() {
        val verificationId = UUID.randomUUID()
        val query = "CJQUNXGW"

        val verificationEntity = VerificationEntity(
            verificationId = verificationId,
            queryText = query,
            timestamp = OffsetDateTime.now(),
            result = objectMapper.writeValueAsString(
                ResultDTO(null, emptyList(), ServiceStatus.SUCCESS)
            ),
            source = CompanySource.FREE,
        )
        verificationRepository.save(verificationEntity)

        val response = testRestTemplate.getForEntity<VerificationDTO>(
            "/verifications/{id}",
            verificationId
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        val body = response.body!!
        assertThat(body.verificationId).isEqualTo(verificationId)
        assertThat(body.queryText).isEqualTo(query)
        assertThat(body.source).isEqualTo(CompanySource.FREE)
        assertThat(body.timestamp).isNotNull()
    }

    @Test
    fun `should return error when verification is not found`() {
        val response = testRestTemplate.getForEntity<ErrorResponse>(
            "/verifications/{id}",
            UUID.randomUUID()
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
        val body = response.body!!
        assertThat(body.message).isEqualTo("Verification not found")
        assertThat(body.status).isEqualTo(404)
    }
}
