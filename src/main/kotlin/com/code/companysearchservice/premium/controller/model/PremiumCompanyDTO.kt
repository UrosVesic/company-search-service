package com.code.companysearchservice.premium.controller.model

import java.time.LocalDate

data class PremiumCompanyDTO(
    val companyIdentificationNumber: String,
    val companyName: String,
    val registrationDate: LocalDate,
    val fullAddress: String,
    val isActive: Boolean
)