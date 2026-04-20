package com.code.companysearchservice.backend.controller

import com.code.companysearchservice.BaseDatabaseIntegrationTest
import com.code.companysearchservice.backend.controller.model.BackendServiceResponseDTO
import com.code.companysearchservice.backend.controller.model.ResultDTO
import com.code.companysearchservice.backend.service.model.CompanySource
import com.code.companysearchservice.backend.service.model.ServiceStatus
import com.code.companysearchservice.backend.service.model.LookupStatus
import com.code.companysearchservice.verification.repository.VerificationRepository
import com.code.companysearchservice.verification.repository.model.VerificationEntity
import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.getForEntity
import org.springframework.http.HttpStatus
import java.time.Instant
import java.util.UUID

class BackendServiceControllerIT(
    @Autowired var verificationRepository: VerificationRepository,
    @Autowired val objectMapper: ObjectMapper
) : BaseDatabaseIntegrationTest() {

    @Test
    fun `should return result from free service when active company is found`() {
        val verificationId = UUID.randomUUID()

        val response = testRestTemplate.getForEntity<BackendServiceResponseDTO>(
            "/backend-service?verificationId={id}&query={q}",
            verificationId, "CJQUNXGW"
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        val body = response.body!!
        assertThat(body.verificationId).isEqualTo(verificationId)
        assertThat(body.query).isEqualTo("CJQUNXGW")

        val result = body.result
        assertThat(result.primaryResult).isNotNull
        val primaryResult = result.primaryResult!!
        assertThat(primaryResult.cin).isEqualTo("CJQUNXGW")
        assertThat(primaryResult.name).isEqualTo("Ramirez-Sanchez")
        assertThat(primaryResult.isActive).isTrue()
        assertThat(result.otherResults).isEmpty()
        assertThat(result.status).isEqualTo(ServiceStatus.SUCCESS)
        assertThat(body.lookupStatus).isEqualTo(LookupStatus.FRESH)
    }

    @Test
    fun `should fall back to premium service when free service returns no active companies`() {
        val verificationId = UUID.randomUUID()

        val response = testRestTemplate.getForEntity<BackendServiceResponseDTO>(
            "/backend-service?verificationId={id}&query={q}",
            verificationId, "LDL93LOZ"
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        val body = response.body!!
        assertThat(body.verificationId).isEqualTo(verificationId)
        assertThat(body.result).isNotNull

        val actual = body.result
        assertThat(actual.primaryResult).isNotNull
        val primaryResult = actual.primaryResult!!
        assertThat(primaryResult.name).isEqualTo("Bolton-Gutierrez")
        assertThat(primaryResult.isActive).isTrue()
        assertThat(actual.status).isEqualTo(ServiceStatus.SUCCESS)
        assertThat(body.lookupStatus).isEqualTo(LookupStatus.FRESH)
    }

    @Test
    fun `should return cached result when verificationId already exists`() {
        val verificationId = UUID.randomUUID()
        val query = "CJQUNXGW"

        verificationRepository.save(
            VerificationEntity(
                verificationId = verificationId,
                queryText = query,
                timestamp = Instant.now(),
                result = objectMapper.writeValueAsString(ResultDTO(null, emptyList(), ServiceStatus.SUCCESS)),
                source = CompanySource.FREE
            )
        )
        val result = testRestTemplate.getForEntity<BackendServiceResponseDTO>(
            "/backend-service?verificationId={id}&query={q}",
            verificationId, "DifferentQuery"
        )

        assertThat(result.statusCode).isEqualTo(HttpStatus.OK)

        val body = result.body!!
        assertThat(body.verificationId).isEqualTo(verificationId)
        assertThat(body.query).isEqualTo(query)
        assertThat(body.result.primaryResult).isEqualTo(null)
        assertThat(body.lookupStatus).isEqualTo(LookupStatus.CACHED)
    }

    @Test
    fun `should return empty result when no companies found in either service`() {
        val verificationId = UUID.randomUUID()

        val response = testRestTemplate.getForEntity<BackendServiceResponseDTO>(
            "/backend-service?verificationId={id}&query={q}",
            verificationId, "ZZZZZZZZZ"
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        val body = response.body!!
        assertThat(body.verificationId).isEqualTo(verificationId)
        assertThat(body.result.primaryResult).isNull()
        assertThat(body.result.otherResults).isEmpty()
        assertThat(body.result.status).isEqualTo(ServiceStatus.EMPTY)
        assertThat(body.lookupStatus).isEqualTo(LookupStatus.FRESH)
    }
}
