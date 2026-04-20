package com.code.companysearchservice.free.controller

import com.code.companysearchservice.common.exception.GlobalExceptionHandler
import com.code.companysearchservice.free.controller.model.FreeCompanyDTO
import com.code.companysearchservice.free.service.FreeThirdPartyService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate

@ActiveProfiles("test")
@SpringBootTest(
    webEnvironment = WebEnvironment.RANDOM_PORT,
    classes = [FreeThirdPartyController::class, FreeThirdPartyService::class, GlobalExceptionHandler::class]
)
@EnableAutoConfiguration(exclude = [DataSourceAutoConfiguration::class, HibernateJpaAutoConfiguration::class])
class FreeThirdPartyControllerIT {

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Test
    fun `should return companies in snake_case format when service is available`() {
        val response = restTemplate.exchange(
            "/free-third-party?query={query}",
            HttpMethod.GET,
            null,
            object : ParameterizedTypeReference<List<FreeCompanyDTO>>() {},
            "CJQUNXGW"
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        val body = response.body!!
        assertThat(body).hasSize(1)
        assertThat(body[0].cin).isEqualTo("CJQUNXGW")
        assertThat(body[0].name).isEqualTo("Ramirez-Sanchez")
        assertThat(body[0].registrationDate).isEqualTo(LocalDate.of(2023, 6, 9))
        assertThat(body[0].isActive).isTrue()
    }

    @Test
    fun `should return empty list when no companies match the query`() {
        val response = restTemplate.exchange(
            "/free-third-party?query={query}",
            HttpMethod.GET,
            null,
            object : ParameterizedTypeReference<List<FreeCompanyDTO>>() {},
            "NONEXISTENT"
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).isEmpty()
    }
}
