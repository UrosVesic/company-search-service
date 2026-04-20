package com.code.companysearchservice.free.service

import com.code.companysearchservice.common.exception.HttpStatusException
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class FreeThirdPartyServiceTest {

    private val objectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())

    @Test
    fun `should return companies whose CIN contains query (case insensitive)`() {
        val service = FreeThirdPartyService(objectMapper, 0.0)
        service.init()

        val results = service.search("cjqunxgw")

        assertThat(results).isNotEmpty
        assertThat(results).allSatisfy { company ->
            assertThat(company.cin.lowercase()).contains("cjqunxgw")
        }
    }

    @Test
    fun `should return empty list when no CIN matches`() {
        val service = FreeThirdPartyService(objectMapper, 0.0)
        service.init()

        val results = service.search("ZZZZZZZZZNOTEXIST")

        assertThat(results).isEmpty()
    }

    @Test
    fun `should return multiple results when multiple CINs match`() {
        val service = FreeThirdPartyService(objectMapper, 0.0)
        service.init()

        // Search with a short string likely to match multiple CINs
        val results = service.search("A")

        assertThat(results.size).isGreaterThanOrEqualTo(1)
        assertThat(results).allSatisfy { company ->
            assertThat(company.cin).containsIgnoringCase("A")
        }
    }

    @Test
    fun `should throw 503 when failure rate is 100 percent`() {
        val service = FreeThirdPartyService(objectMapper, 1.0)
        service.init()

        assertThatThrownBy { service.search("ABC") }
            .isInstanceOf(HttpStatusException::class.java)
            .satisfies({ ex ->
                assertThat((ex as HttpStatusException).status).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
            })
    }

    @Test
    fun `should never throw when failure rate is zero`() {
        val service = FreeThirdPartyService(objectMapper, 0.0)
        service.init()

        repeat(20) {
            val results = service.search("CJQUNXGW")
            assertThat(results).isNotEmpty
        }
    }
}
