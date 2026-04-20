package com.code.companysearchservice.premium.service

import com.code.companysearchservice.common.exception.HttpStatusException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.code.companysearchservice.premium.controller.model.PremiumCompanyDTO
import jakarta.annotation.PostConstruct
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpStatus
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import kotlin.random.Random

@Service
class PremiumThirdPartyService(
    private val objectMapper: ObjectMapper,
    @Value("\${app.premium-service.failure-rate}") private val failureRate: Double
) {

    private lateinit var companies: List<PremiumCompanyDTO>

    @PostConstruct
    fun init() {
        val resource = ClassPathResource("data/premium_service_companies.json")
        companies = objectMapper.readValue<List<PremiumCompanyDTO>>(resource.inputStream)
    }

    fun search(query: String): List<PremiumCompanyDTO> {
        if (Random.nextDouble() < failureRate) {
            throw HttpStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Premium service is currently unavailable")
        }
        return companies.filter {
            it.companyIdentificationNumber.contains(query, ignoreCase = true)
        }
    }
}
