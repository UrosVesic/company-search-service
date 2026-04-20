package com.code.companysearchservice.free.service

import com.code.companysearchservice.common.exception.HttpStatusException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.code.companysearchservice.free.controller.model.FreeCompanyDTO
import jakarta.annotation.PostConstruct
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpStatus
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import kotlin.random.Random

@Service
class FreeThirdPartyService(
    private val objectMapper: ObjectMapper,
    @Value("\${app.free-service.failure-rate}") private val failureRate: Double
) {

    private lateinit var companies: List<FreeCompanyDTO>

    @PostConstruct
    fun init() {
        val resource = ClassPathResource("data/free_service_companies.json")
        companies = objectMapper.readValue<List<FreeCompanyDTO>>(resource.inputStream)
    }

    fun search(query: String): List<FreeCompanyDTO> {
        if (Random.nextDouble() < failureRate) {
            throw HttpStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Free service is currently unavailable")
        }
        return companies.filter { it.cin.contains(query, ignoreCase = true) }
    }
}
