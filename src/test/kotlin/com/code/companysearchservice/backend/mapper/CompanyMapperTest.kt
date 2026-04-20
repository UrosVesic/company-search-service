package com.code.companysearchservice.backend.mapper

import com.code.companysearchservice.backend.controller.model.CompanyDTO
import com.code.companysearchservice.backend.service.model.CompanySearchResult
import com.code.companysearchservice.backend.service.model.CompanySource
import com.code.companysearchservice.backend.service.model.ServiceStatus
import com.code.companysearchservice.free.controller.model.FreeCompanyDTO
import com.code.companysearchservice.premium.controller.model.PremiumCompanyDTO
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate

class CompanyMapperTest {

    @Nested
    inner class FreeCompanyDTOMapping {

        @Test
        fun `should map FreeCompanyDTO to CompanyDTO`() {
            val free = FreeCompanyDTO(
                cin = "ABC123",
                name = "Test Company",
                registrationDate = LocalDate.of(2023, 6, 15),
                address = "123 Main St",
                isActive = true
            )

            val result = free.toCompanyDTO()

            assertThat(result.cin).isEqualTo("ABC123")
            assertThat(result.name).isEqualTo("Test Company")
            assertThat(result.registrationDate).isEqualTo(LocalDate.of(2023, 6, 15))
            assertThat(result.address).isEqualTo("123 Main St")
            assertThat(result.isActive).isTrue()
        }
    }

    @Nested
    inner class PremiumCompanyDTOMapping {

        @Test
        fun `should map PremiumCompanyDTO to CompanyDTO with field name translation`() {
            val premium = PremiumCompanyDTO(
                companyIdentificationNumber = "XYZ789",
                companyName = "Premium Corp",
                registrationDate = LocalDate.of(2024, 1, 10),
                fullAddress = "456 Premium Ave",
                isActive = false
            )

            val result = premium.toCompanyDTO()

            assertThat(result.cin).isEqualTo("XYZ789")
            assertThat(result.name).isEqualTo("Premium Corp")
            assertThat(result.registrationDate).isEqualTo(LocalDate.of(2024, 1, 10))
            assertThat(result.address).isEqualTo("456 Premium Ave")
            assertThat(result.isActive).isFalse()
        }
    }

    @Nested
    inner class ToResultDTO {

        @Test
        fun `should return first company as primary and rest as others`() {
            val companies = listOf(company("A"), company("B"), company("C"))
            val searchResult = CompanySearchResult(companies, CompanySource.FREE, ServiceStatus.SUCCESS, ServiceStatus.NOT_CALLED)

            val result = searchResult.toResultDTO()

            assertThat(result.primaryResult!!.cin).isEqualTo("A")
            assertThat(result.otherResults).hasSize(2)
            assertThat(result.otherResults.map { it.cin }).containsExactly("B", "C")
            assertThat(result.status).isEqualTo(ServiceStatus.SUCCESS)
        }

        @Test
        fun `should return single company as primary with empty others`() {
            val searchResult = CompanySearchResult(
                listOf(company("ONLY")), CompanySource.FREE, ServiceStatus.SUCCESS, ServiceStatus.NOT_CALLED
            )

            val result = searchResult.toResultDTO()

            assertThat(result.primaryResult!!.cin).isEqualTo("ONLY")
            assertThat(result.otherResults).isEmpty()
        }

        @Test
        fun `should return null primary and empty others when no companies`() {
            val searchResult = CompanySearchResult(
                emptyList(), CompanySource.FREE, ServiceStatus.EMPTY, ServiceStatus.EMPTY
            )

            val result = searchResult.toResultDTO()

            assertThat(result.primaryResult).isNull()
            assertThat(result.otherResults).isEmpty()
            assertThat(result.status).isEqualTo(ServiceStatus.EMPTY)
        }

        @Test
        fun `should return UNAVAILABLE status when source is UNAVAILABLE`() {
            val searchResult = CompanySearchResult(
                emptyList(), CompanySource.UNAVAILABLE, ServiceStatus.UNAVAILABLE, ServiceStatus.UNAVAILABLE
            )

            val result = searchResult.toResultDTO()

            assertThat(result.primaryResult).isNull()
            assertThat(result.status).isEqualTo(ServiceStatus.UNAVAILABLE)
        }
    }

    @Nested
    inner class CalculatedFinalStatus {

        @Test
        fun `should return SUCCESS when primary result exists`() {
            val status = calculatedFinalStatus(company("A"), ServiceStatus.SUCCESS, ServiceStatus.NOT_CALLED)
            assertThat(status).isEqualTo(ServiceStatus.SUCCESS)
        }

        @Test
        fun `should return UNAVAILABLE when both services unavailable and no result`() {
            val status = calculatedFinalStatus(null, ServiceStatus.UNAVAILABLE, ServiceStatus.UNAVAILABLE)
            assertThat(status).isEqualTo(ServiceStatus.UNAVAILABLE)
        }

        @Test
        fun `should return EMPTY when no result but at least one service responded`() {
            val status = calculatedFinalStatus(null, ServiceStatus.EMPTY, ServiceStatus.EMPTY)
            assertThat(status).isEqualTo(ServiceStatus.EMPTY)
        }

        @Test
        fun `should return EMPTY when no result and free unavailable but premium responded`() {
            val status = calculatedFinalStatus(null, ServiceStatus.UNAVAILABLE, ServiceStatus.EMPTY)
            assertThat(status).isEqualTo(ServiceStatus.EMPTY)
        }

        @Test
        fun `should return EMPTY when no result and free responded but premium unavailable`() {
            val status = calculatedFinalStatus(null, ServiceStatus.EMPTY, ServiceStatus.UNAVAILABLE)
            assertThat(status).isEqualTo(ServiceStatus.EMPTY)
        }
    }

    private fun company(cin: String) = CompanyDTO(
        cin = cin, name = "Company $cin", registrationDate = LocalDate.of(2023, 1, 1),
        address = "Address", isActive = true
    )
}
