package com.code.companysearchservice.backend.mapper

import com.code.companysearchservice.backend.controller.model.CompanyDTO
import com.code.companysearchservice.backend.controller.model.ResultDTO
import com.code.companysearchservice.backend.service.model.CompanySearchResult
import com.code.companysearchservice.free.controller.model.FreeCompanyDTO
import com.code.companysearchservice.premium.controller.model.PremiumCompanyDTO
import com.code.companysearchservice.backend.service.model.CompanySource
import com.code.companysearchservice.backend.service.model.ServiceStatus

fun FreeCompanyDTO.toCompanyDTO() = CompanyDTO(
    cin = cin,
    name = name,
    registrationDate = registrationDate,
    address = address,
    isActive = isActive
)

fun PremiumCompanyDTO.toCompanyDTO() = CompanyDTO(
    cin = companyIdentificationNumber,
    name = companyName,
    registrationDate = registrationDate,
    address = fullAddress,
    isActive = isActive
)

fun CompanySearchResult.toResultDTO(): ResultDTO =
    when (source) {
        CompanySource.UNAVAILABLE -> {
            val status = calculatedFinalStatus(null, freeStatus, premiumStatus)
            ResultDTO(null, emptyList(), status)
        }

        else -> {
            val first = companies.firstOrNull()
            val rest = companies.drop(1)
            val status = calculatedFinalStatus(first, freeStatus, premiumStatus)
            ResultDTO(first, rest, status)
        }
    }

fun calculatedFinalStatus(
    primaryResult: CompanyDTO?,
    freeServiceStatus: ServiceStatus,
    premiumServiceStatus: ServiceStatus
): ServiceStatus {
    if (primaryResult != null) {
        return ServiceStatus.SUCCESS
    }

    if (freeServiceStatus == ServiceStatus.UNAVAILABLE && premiumServiceStatus == ServiceStatus.UNAVAILABLE) {
        return ServiceStatus.UNAVAILABLE
    }

    return ServiceStatus.EMPTY
}