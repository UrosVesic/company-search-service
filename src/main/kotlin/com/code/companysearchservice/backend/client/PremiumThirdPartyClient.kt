package com.code.companysearchservice.backend.client

import com.code.companysearchservice.backend.client.model.ClientResult
import com.code.companysearchservice.backend.controller.model.CompanyDTO
import com.code.companysearchservice.backend.mapper.toCompanyDTO
import com.code.companysearchservice.common.exception.HttpStatusException
import com.code.companysearchservice.premium.controller.model.PremiumCompanyDTO
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

@Component
class PremiumThirdPartyClient(
    private val restClient: RestClient
) {

    fun searchCompanies(query: String): ClientResult<List<CompanyDTO>> {
        return try {
            val body = restClient.get()
                .uri("/premium-third-party?query={q}", query)
                .retrieve()
                .body<List<PremiumCompanyDTO>>()
                ?.map { it.toCompanyDTO() }
                ?: throw HttpStatusException(HttpStatus.BAD_GATEWAY, "Empty response")
            ClientResult.Success(body)
        } catch (_: HttpServerErrorException) {
            ClientResult.ServiceUnavailable
        }
    }
}
