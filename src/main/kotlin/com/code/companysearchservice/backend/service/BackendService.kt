package com.code.companysearchservice.backend.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.code.companysearchservice.backend.client.FreeThirdPartyClient
import com.code.companysearchservice.backend.client.PremiumThirdPartyClient
import com.code.companysearchservice.backend.client.model.ClientResult
import com.code.companysearchservice.backend.client.model.toServiceStatus
import com.code.companysearchservice.backend.controller.model.BackendServiceResponseDTO
import com.code.companysearchservice.backend.controller.model.ResultDTO
import com.code.companysearchservice.backend.mapper.toResultDTO
import com.code.companysearchservice.backend.service.model.CompanySearchResult
import com.code.companysearchservice.backend.service.model.CompanySource
import com.code.companysearchservice.backend.service.model.ServiceStatus
import com.code.companysearchservice.backend.service.model.LookupStatus
import com.code.companysearchservice.verification.repository.VerificationRepository
import com.code.companysearchservice.verification.repository.model.VerificationEntity
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.util.UUID

@Service
class BackendService(
    private val freeThirdPartyClient: FreeThirdPartyClient,
    private val premiumThirdPartyClient: PremiumThirdPartyClient,
    private val verificationRepository: VerificationRepository,
    private val objectMapper: ObjectMapper
) {

    fun search(verificationId: UUID, query: String): BackendServiceResponseDTO {
        verificationRepository.findByVerificationId(verificationId)?.let {
            return it.toCachedResponse(verificationId)
        }

        val resolvedResult = resolveCompanies(query)
        val resultDTO = resolvedResult.toResultDTO()

        val id = UUID.randomUUID()
        val saved = verificationRepository.upsert(
            VerificationEntity(
                id = id,
                verificationId = verificationId,
                queryText = query,
                timestamp = OffsetDateTime.now(),
                result = objectMapper.writeValueAsString(resultDTO),
                source = resolvedResult.source,
            )
        )

        return BackendServiceResponseDTO(
            verificationId = verificationId,
            query = query,
            result = resultDTO,
            lookupStatus = if (saved.id != id) LookupStatus.CACHED else LookupStatus.FRESH,
        )
    }

    private fun VerificationEntity.toCachedResponse(verificationId: UUID) =
        BackendServiceResponseDTO(
            verificationId = verificationId,
            query = queryText,
            result = objectMapper.readValue<ResultDTO>(result),
            lookupStatus = LookupStatus.CACHED,
        )

    private fun resolveCompanies(query: String): CompanySearchResult {
        val freeResult = freeThirdPartyClient.searchCompanies(query)
        val freeStatus = freeResult.toServiceStatus()

        if (freeResult is ClientResult.Success && freeResult.data.any { it.isActive }) {
            val active = freeResult.data.filter { it.isActive }
            return CompanySearchResult(active, CompanySource.FREE, freeStatus, ServiceStatus.NOT_CALLED)
        }

        val premiumResult = premiumThirdPartyClient.searchCompanies(query)
        val premiumStatus = premiumResult.toServiceStatus()

        if (premiumResult is ClientResult.Success) {
            val active = premiumResult.data.filter { it.isActive }
            return CompanySearchResult(active, CompanySource.PREMIUM, freeStatus, premiumStatus)
        }

        return CompanySearchResult(emptyList(), CompanySource.UNAVAILABLE, freeStatus, premiumStatus)
    }
}
