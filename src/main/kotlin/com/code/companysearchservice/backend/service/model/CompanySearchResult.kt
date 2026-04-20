package com.code.companysearchservice.backend.service.model

import com.code.companysearchservice.backend.controller.model.CompanyDTO

data class CompanySearchResult(
    val companies: List<CompanyDTO>,
    val source: CompanySource,
    val freeStatus: ServiceStatus,
    val premiumStatus: ServiceStatus
)