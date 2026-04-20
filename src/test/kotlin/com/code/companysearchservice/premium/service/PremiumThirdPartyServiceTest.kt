package com.code.companysearchservice.premium.service

import com.code.companysearchservice.common.exception.HttpStatusException
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class PremiumThirdPartyServiceTest {

    private val objectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())

    @Test
    fun `should return companies matching CIN`() {
        val service = PremiumThirdPartyService(objectMapper, 0.0)
        service.init()

        val results = service.search("CJQUNXGW")

        assertThat(results).isNotEmpty
        assertThat(results).allSatisfy { company ->
            assertThat(
                company.companyIdentificationNumber.contains("CJQUNXGW", ignoreCase = true)
            ).isTrue()
        }
    }

    @Test
    fun `should return companies matching company name`() {
        val service = PremiumThirdPartyService(objectMapper, 0.0)
        service.init()

        val results = service.search("LDL93LOZ")

        assertThat(results).isNotEmpty
        assertThat(results).allSatisfy { company ->
            assertThat(
                company.companyIdentificationNumber.contains("LDL93LOZ", ignoreCase = true)
            ).isTrue()
        }
    }

    @Test
    fun `should return empty list when nothing matches`() {
        val service = PremiumThirdPartyService(objectMapper, 0.0)
        service.init()

        val results = service.search("ZZZZZZZZZNOTEXIST")

        assertThat(results).isEmpty()
    }

    @Test
    fun `should search case insensitively`() {
        val service = PremiumThirdPartyService(objectMapper, 0.0)
        service.init()

        val upper = service.search("BOLTON")
        val lower = service.search("bolton")

        assertThat(upper).isEqualTo(lower)
    }

    @Test
    fun `should throw 503 when failure rate is 100 percent`() {
        val service = PremiumThirdPartyService(objectMapper, 1.0)
        service.init()

        assertThatThrownBy { service.search("ABC") }
            .isInstanceOf(HttpStatusException::class.java)
            .satisfies({ ex ->
                assertThat((ex as HttpStatusException).status).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
            })
    }

    @Test
    fun `should never throw when failure rate is zero`() {
        val service = PremiumThirdPartyService(objectMapper, 0.0)
        service.init()

        repeat(20) {
            service.search("Bolton")
        }
    }
}
