package com.code.companysearchservice.verification.service

import com.code.companysearchservice.backend.service.model.CompanySource
import com.code.companysearchservice.common.exception.HttpStatusException
import com.code.companysearchservice.verification.mapper.VerificationMapper
import com.code.companysearchservice.verification.repository.VerificationRepository
import com.code.companysearchservice.verification.repository.model.VerificationEntity
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.time.Instant
import java.util.UUID

class VerificationServiceTest {

    private val verificationRepository: VerificationRepository = mockk()
    private val objectMapper: ObjectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())
    private val verificationMapper = VerificationMapper(objectMapper)
    private val verificationService = VerificationService(verificationRepository, verificationMapper)

    @Test
    fun `should return verification DTO when found`() {
        val verificationId = UUID.randomUUID()
        val timestamp = Instant.now()
        val resultJson =
            """{"primaryResult":{"cin":"ABC","name":"Test","registrationDate":"2023-01-01","address":"123 St","isActive":true},"otherResults":[],"status":"SUCCESS"}"""

        every { verificationRepository.findByVerificationId(verificationId) } returns VerificationEntity(
            verificationId = verificationId,
            queryText = "ABC",
            timestamp = timestamp,
            result = resultJson,
            source = CompanySource.FREE
        )

        val result = verificationService.findById(verificationId)

        assertThat(result.verificationId).isEqualTo(verificationId)
        assertThat(result.queryText).isEqualTo("ABC")
        assertThat(result.timestamp).isEqualTo(timestamp)
        assertThat(result.result.primaryResult!!.cin).isEqualTo("ABC")
        assertThat(result.source).isEqualTo(CompanySource.FREE)
    }

    @Test
    fun `should throw HttpStatusException with 404 when verification not found`() {
        val verificationId = UUID.randomUUID()
        every { verificationRepository.findByVerificationId(verificationId) } returns null

        assertThatThrownBy { verificationService.findById(verificationId) }
            .isInstanceOf(HttpStatusException::class.java)
            .satisfies({ ex ->
                assertThat((ex as HttpStatusException).status).isEqualTo(HttpStatus.NOT_FOUND)
            })
    }
}
