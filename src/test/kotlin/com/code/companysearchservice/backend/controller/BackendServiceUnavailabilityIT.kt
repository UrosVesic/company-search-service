package com.code.companysearchservice.backend.controller

import com.code.companysearchservice.BaseDatabaseIntegrationTest
import com.code.companysearchservice.backend.controller.model.BackendServiceResponseDTO
import com.code.companysearchservice.backend.service.model.CompanySource
import com.code.companysearchservice.backend.service.model.ServiceStatus
import com.code.companysearchservice.backend.service.model.LookupStatus
import com.code.companysearchservice.free.service.FreeThirdPartyService
import com.code.companysearchservice.premium.service.PremiumThirdPartyService
import com.code.companysearchservice.verification.repository.VerificationRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.web.client.getForEntity
import org.springframework.http.HttpStatus
import org.springframework.test.util.ReflectionTestUtils
import java.util.UUID

class BackendServiceUnavailabilityIT(
    @Autowired val freeService: FreeThirdPartyService,
    @Autowired val premiumService: PremiumThirdPartyService,
    @Autowired val verificationRepository: VerificationRepository,
    @Value("\${app.free-service.failure-rate}") private val freeFailureRate: Double,
    @Value("\${app.premium-service.failure-rate}") private val premiumFailureRate: Double
) : BaseDatabaseIntegrationTest() {

    @AfterEach
    fun resetFailureRates() {
        ReflectionTestUtils.setField(freeService, "failureRate", freeFailureRate)
        ReflectionTestUtils.setField(premiumService, "failureRate", premiumFailureRate)
    }

    @Test
    fun `should fall back to premium when free service is unavailable`() {
        ReflectionTestUtils.setField(freeService, "failureRate", 1.0)

        val verificationId = UUID.randomUUID()
        val response = testRestTemplate.getForEntity<BackendServiceResponseDTO>(
            "/backend-service?verificationId={id}&query={q}",
            verificationId, "LDL93LOZ"
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        val body = response.body!!
        assertThat(body.verificationId).isEqualTo(verificationId)
        assertThat(body.result.primaryResult).isNotNull
        assertThat(body.result.primaryResult!!.name).isEqualTo("Bolton-Gutierrez")
        assertThat(body.result.primaryResult!!.isActive).isTrue()
        assertThat(body.result.status).isEqualTo(ServiceStatus.SUCCESS)
        assertThat(body.lookupStatus).isEqualTo(LookupStatus.FRESH)

        val verification = verificationRepository.findByVerificationId(verificationId)
        assertThat(verification).isNotNull
        assertThat(verification!!.source).isEqualTo(CompanySource.PREMIUM)
    }

    @Test
    fun `should return empty result when free service is unavailable and premium has no matches`() {
        ReflectionTestUtils.setField(freeService, "failureRate", 1.0)

        val verificationId = UUID.randomUUID()
        val response = testRestTemplate.getForEntity<BackendServiceResponseDTO>(
            "/backend-service?verificationId={id}&query={q}",
            verificationId, "ZZZZZZZZZ"
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        val body = response.body!!
        assertThat(body.result.primaryResult).isNull()
        assertThat(body.result.otherResults).isEmpty()
        assertThat(body.result.status).isEqualTo(ServiceStatus.EMPTY)

        val verification = verificationRepository.findByVerificationId(verificationId)
        assertThat(verification).isNotNull
        assertThat(verification!!.source).isEqualTo(CompanySource.PREMIUM)
    }

    @Test
    fun `should return from free service when premium is unavailable and free has active results`() {
        ReflectionTestUtils.setField(premiumService, "failureRate", 1.0)

        val verificationId = UUID.randomUUID()
        val response = testRestTemplate.getForEntity<BackendServiceResponseDTO>(
            "/backend-service?verificationId={id}&query={q}",
            verificationId, "CJQUNXGW"
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        val body = response.body!!
        assertThat(body.result.primaryResult).isNotNull
        assertThat(body.result.primaryResult!!.cin).isEqualTo("CJQUNXGW")
        assertThat(body.result.primaryResult!!.name).isEqualTo("Ramirez-Sanchez")
        assertThat(body.result.status).isEqualTo(ServiceStatus.SUCCESS)

        val verification = verificationRepository.findByVerificationId(verificationId)
        assertThat(verification).isNotNull
        assertThat(verification!!.source).isEqualTo(CompanySource.FREE)
    }

    @Test
    fun `should return empty when premium is unavailable and free has no active results`() {
        ReflectionTestUtils.setField(premiumService, "failureRate", 1.0)

        val verificationId = UUID.randomUUID()
        val response = testRestTemplate.getForEntity<BackendServiceResponseDTO>(
            "/backend-service?verificationId={id}&query={q}",
            verificationId, "LDL93LOZ"
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        val body = response.body!!
        assertThat(body.result.primaryResult).isNull()
        assertThat(body.result.otherResults).isEmpty()
        assertThat(body.result.status).isEqualTo(ServiceStatus.EMPTY)

        val verification = verificationRepository.findByVerificationId(verificationId)
        assertThat(verification).isNotNull
        assertThat(verification!!.source).isEqualTo(CompanySource.UNAVAILABLE)
    }

    @Test
    fun `should return unavailable status when both services are unavailable`() {
        ReflectionTestUtils.setField(freeService, "failureRate", 1.0)
        ReflectionTestUtils.setField(premiumService, "failureRate", 1.0)

        val verificationId = UUID.randomUUID()
        val response = testRestTemplate.getForEntity<BackendServiceResponseDTO>(
            "/backend-service?verificationId={id}&query={q}",
            verificationId, "CJQUNXGW"
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        val body = response.body!!
        assertThat(body.result.primaryResult).isNull()
        assertThat(body.result.otherResults).isEmpty()
        assertThat(body.result.status).isEqualTo(ServiceStatus.UNAVAILABLE)
        assertThat(body.lookupStatus).isEqualTo(LookupStatus.FRESH)

        val verification = verificationRepository.findByVerificationId(verificationId)
        assertThat(verification).isNotNull
        assertThat(verification!!.source).isEqualTo(CompanySource.UNAVAILABLE)
    }
}
