package com.code.companysearchservice.backend.controller.model

import com.code.companysearchservice.backend.service.model.ServiceStatus

data class ResultDTO(
    val primaryResult: CompanyDTO?,
    val otherResults: List<CompanyDTO> = emptyList(),
    val status: ServiceStatus
)