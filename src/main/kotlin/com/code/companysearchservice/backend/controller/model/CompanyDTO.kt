package com.code.companysearchservice.backend.controller.model

import java.time.LocalDate

data class CompanyDTO(
    val cin: String,
    val name: String,
    val registrationDate: LocalDate,
    val address: String,
    val isActive: Boolean
)