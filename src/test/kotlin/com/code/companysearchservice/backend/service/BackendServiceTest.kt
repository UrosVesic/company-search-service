package com.code.companysearchservice.backend.service

import com.code.companysearchservice.backend.client.FreeThirdPartyClient
import com.code.companysearchservice.backend.client.PremiumThirdPartyClient
import com.code.companysearchservice.backend.client.model.ClientResult
import com.code.companysearchservice.backend.controller.model.CompanyDTO
import com.code.companysearchservice.backend.service.model.CompanySource
import com.code.companysearchservice.backend.service.model.ServiceStatus
import com.code.companysearchservice.backend.service.model.LookupStatus
import com.code.companysearchservice.verification.repository.VerificationRepository
import com.code.companysearchservice.verification.repository.model.VerificationEntity
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

class BackendServiceTest {

    private val freeClient: FreeThirdPartyClient = mockk()
    private val premiumClient: PremiumThirdPartyClient = mockk()
    private val verificationRepository: VerificationRepository = mockk()
    private val objectMapper: ObjectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())

    private lateinit var backendService: BackendService

    @BeforeEach
    fun setUp() {
        every { verificationRepository.save(any()) } answers { firstArg() }
        backendService = BackendService(freeClient, premiumClient, verificationRepository, objectMapper)
    }

    @Test
    fun `should return result from free service when active companies found`() {
        val verificationId = UUID.randomUUID()
        every { verificationRepository.findByVerificationId(verificationId) } returns null
        every { freeClient.searchCompanies("ABC") } returns ClientResult.Success(
            listOf(activeCompany("ABC", "Company A"), inactiveCompany("ABC2", "Company B"))
        )

        val response = backendService.search(verificationId, "ABC")

        assertThat(response.verificationId).isEqualTo(verificationId)
        assertThat(response.query).isEqualTo("ABC")
        assertThat(response.result.primaryResult).isNotNull
        assertThat(response.result.primaryResult!!.cin).isEqualTo("ABC")
        assertThat(response.result.otherResults).isEmpty()
        assertThat(response.result.status).isEqualTo(ServiceStatus.SUCCESS)
        assertThat(response.lookupStatus).isEqualTo(LookupStatus.FRESH)
        verify(exactly = 0) { premiumClient.searchCompanies(any()) }
    }

    @Test
    fun `should include other active results when multiple active companies found`() {
        val verificationId = UUID.randomUUID()
        every { verificationRepository.findByVerificationId(verificationId) } returns null
        every { freeClient.searchCompanies("ABC") } returns ClientResult.Success(
            listOf(
                activeCompany("ABC1", "Company A"),
                activeCompany("ABC2", "Company B"),
                activeCompany("ABC3", "Company C")
            )
        )

        val response = backendService.search(verificationId, "ABC")

        assertThat(response.result.primaryResult!!.cin).isEqualTo("ABC1")
        assertThat(response.result.otherResults).hasSize(2)
        assertThat(response.result.otherResults.map { it.cin }).containsExactly("ABC2", "ABC3")
    }

    @Test
    fun `should fall back to premium when free service returns ServiceUnavailable`() {
        val verificationId = UUID.randomUUID()
        every { verificationRepository.findByVerificationId(verificationId) } returns null
        every { freeClient.searchCompanies("XYZ") } returns ClientResult.ServiceUnavailable
        every { premiumClient.searchCompanies("XYZ") } returns ClientResult.Success(
            listOf(activeCompany("XYZ", "Premium Co"))
        )

        val response = backendService.search(verificationId, "XYZ")

        assertThat(response.result.primaryResult!!.cin).isEqualTo("XYZ")
        assertThat(response.result.primaryResult!!.name).isEqualTo("Premium Co")
    }

    @Test
    fun `should fall back to premium when free service returns no active companies`() {
        val verificationId = UUID.randomUUID()
        every { verificationRepository.findByVerificationId(verificationId) } returns null
        every { freeClient.searchCompanies("QUERY") } returns ClientResult.Success(
            listOf(inactiveCompany("Q1", "Inactive Co"))
        )
        every { premiumClient.searchCompanies("QUERY") } returns ClientResult.Success(
            listOf(activeCompany("Q2", "Active Premium"))
        )

        val response = backendService.search(verificationId, "QUERY")

        assertThat(response.result.primaryResult!!.name).isEqualTo("Active Premium")
    }

    @Test
    fun `should fall back to premium when free service returns empty list`() {
        val verificationId = UUID.randomUUID()
        every { verificationRepository.findByVerificationId(verificationId) } returns null
        every { freeClient.searchCompanies("QUERY") } returns ClientResult.Success(emptyList())
        every { premiumClient.searchCompanies("QUERY") } returns ClientResult.Success(
            listOf(activeCompany("P1", "Premium Result"))
        )

        val response = backendService.search(verificationId, "QUERY")

        assertThat(response.result.primaryResult!!.name).isEqualTo("Premium Result")
    }

    @Test
    fun `should return unavailable status when both services fail`() {
        val verificationId = UUID.randomUUID()
        every { verificationRepository.findByVerificationId(verificationId) } returns null
        every { freeClient.searchCompanies("Q") } returns ClientResult.ServiceUnavailable
        every { premiumClient.searchCompanies("Q") } returns ClientResult.ServiceUnavailable

        val response = backendService.search(verificationId, "Q")

        assertThat(response.result.primaryResult).isNull()
        assertThat(response.result.otherResults).isEmpty()
        assertThat(response.result.status).isEqualTo(ServiceStatus.UNAVAILABLE)
    }

    @Test
    fun `should return empty status when both services return no results`() {
        val verificationId = UUID.randomUUID()
        every { verificationRepository.findByVerificationId(verificationId) } returns null
        every { freeClient.searchCompanies("NOPE") } returns ClientResult.Success(emptyList())
        every { premiumClient.searchCompanies("NOPE") } returns ClientResult.Success(emptyList())

        val response = backendService.search(verificationId, "NOPE")

        assertThat(response.result.primaryResult).isNull()
        assertThat(response.result.status).isEqualTo(ServiceStatus.EMPTY)
    }

    @Test
    fun `should return cached result when verificationId already exists`() {
        val verificationId = UUID.randomUUID()
        val resultJson = objectMapper.writeValueAsString(
            mapOf(
                "primaryResult" to mapOf(
                    "cin" to "OLD",
                    "name" to "Old Company",
                    "registrationDate" to "2023-01-01",
                    "address" to "123 Old St",
                    "isActive" to true
                ),
                "otherResults" to emptyList<Any>(),
                "status" to "SUCCESS"
            )
        )
        every { verificationRepository.findByVerificationId(verificationId) } returns VerificationEntity(
            verificationId = verificationId,
            queryText = "OLD",
            timestamp = Instant.now(),
            result = resultJson,
            source = CompanySource.FREE
        )

        val response = backendService.search(verificationId, "DIFFERENT_QUERY")

        assertThat(response.verificationId).isEqualTo(verificationId)
        assertThat(response.query).isEqualTo("OLD")
        assertThat(response.result.primaryResult!!.cin).isEqualTo("OLD")
        assertThat(response.lookupStatus).isEqualTo(LookupStatus.CACHED)
        verify(exactly = 0) { freeClient.searchCompanies(any()) }
        verify(exactly = 0) { premiumClient.searchCompanies(any()) }
    }

    @Test
    fun `should store verification with correct source when free service used`() {
        val verificationId = UUID.randomUUID()
        val entitySlot = slot<VerificationEntity>()
        every { verificationRepository.findByVerificationId(verificationId) } returns null
        every { freeClient.searchCompanies("ABC") } returns ClientResult.Success(
            listOf(activeCompany("ABC", "Free Co"))
        )
        every { verificationRepository.save(capture(entitySlot)) } answers { entitySlot.captured }

        backendService.search(verificationId, "ABC")

        val saved = entitySlot.captured
        assertThat(saved.verificationId).isEqualTo(verificationId)
        assertThat(saved.queryText).isEqualTo("ABC")
        assertThat(saved.source).isEqualTo(CompanySource.FREE)
        assertThat(saved.timestamp).isNotNull()
        assertThat(saved.result).contains("ABC")
    }

    @Test
    fun `should store verification with PREMIUM source when fallback used`() {
        val verificationId = UUID.randomUUID()
        val entitySlot = slot<VerificationEntity>()
        every { verificationRepository.findByVerificationId(verificationId) } returns null
        every { freeClient.searchCompanies("Q") } returns ClientResult.ServiceUnavailable
        every { premiumClient.searchCompanies("Q") } returns ClientResult.Success(
            listOf(activeCompany("Q1", "Premium Co"))
        )
        every { verificationRepository.save(capture(entitySlot)) } answers { entitySlot.captured }

        backendService.search(verificationId, "Q")

        assertThat(entitySlot.captured.source).isEqualTo(CompanySource.PREMIUM)
    }

    @Test
    fun `should store verification with UNAVAILABLE source when both services fail`() {
        val verificationId = UUID.randomUUID()
        val entitySlot = slot<VerificationEntity>()
        every { verificationRepository.findByVerificationId(verificationId) } returns null
        every { freeClient.searchCompanies("Q") } returns ClientResult.ServiceUnavailable
        every { premiumClient.searchCompanies("Q") } returns ClientResult.ServiceUnavailable
        every { verificationRepository.save(capture(entitySlot)) } answers { entitySlot.captured }

        backendService.search(verificationId, "Q")

        assertThat(entitySlot.captured.source).isEqualTo(CompanySource.UNAVAILABLE)
    }

    @Test
    fun `should filter out inactive companies from premium results`() {
        val verificationId = UUID.randomUUID()
        every { verificationRepository.findByVerificationId(verificationId) } returns null
        every { freeClient.searchCompanies("Q") } returns ClientResult.ServiceUnavailable
        every { premiumClient.searchCompanies("Q") } returns ClientResult.Success(
            listOf(
                inactiveCompany("Q1", "Inactive"),
                activeCompany("Q2", "Active"),
                inactiveCompany("Q3", "Also Inactive")
            )
        )

        val response = backendService.search(verificationId, "Q")

        assertThat(response.result.primaryResult!!.cin).isEqualTo("Q2")
        assertThat(response.result.otherResults).isEmpty()
    }

    private fun activeCompany(cin: String, name: String) = CompanyDTO(
        cin = cin, name = name, registrationDate = LocalDate.of(2023, 1, 1),
        address = "123 Test St", isActive = true
    )

    private fun inactiveCompany(cin: String, name: String) = CompanyDTO(
        cin = cin, name = name, registrationDate = LocalDate.of(2023, 1, 1),
        address = "456 Test St", isActive = false
    )
}
