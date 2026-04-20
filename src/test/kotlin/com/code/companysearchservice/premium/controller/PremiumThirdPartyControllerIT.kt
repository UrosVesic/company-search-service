package com.code.companysearchservice.premium.controller

import com.code.companysearchservice.common.exception.GlobalExceptionHandler
import com.code.companysearchservice.premium.controller.model.PremiumCompanyDTO
import com.code.companysearchservice.premium.service.PremiumThirdPartyService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.getForEntity
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@SpringBootTest(
    webEnvironment = WebEnvironment.RANDOM_PORT,
    classes = [PremiumThirdPartyController::class, PremiumThirdPartyService::class, GlobalExceptionHandler::class]
)
@EnableAutoConfiguration(exclude = [DataSourceAutoConfiguration::class, HibernateJpaAutoConfiguration::class])
class PremiumThirdPartyControllerIT {

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Test
    fun `should return companies in camelCase format when service is available`() {
        val body = restTemplate.exchange(
            "/premium-third-party?query={query}",
            HttpMethod.GET,
            null,
            object : ParameterizedTypeReference<List<PremiumCompanyDTO>>() {},
            "CJQUNXGW"
        ).body!!

        val result = body[0]
        assertThat(result.companyIdentificationNumber).isEqualTo("CJQUNXGW")
        assertThat(result.companyName).isEqualTo("Ramirez-Sanchez")
        assertThat(result.registrationDate).isEqualTo("2023-06-09")
        assertThat(result.isActive).isEqualTo(true)
    }

    @Test
    fun `should return empty list when no companies match the query`() {
        val response = restTemplate.getForEntity<List<PremiumCompanyDTO>>(
            "/premium-third-party?query={query}",
            "NONEXISTENT"
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).isEmpty()
    }
}
