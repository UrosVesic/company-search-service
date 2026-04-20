package com.code.companysearchservice.free.controller.model

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.time.LocalDate

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class FreeCompanyDTO(
    val cin: String,
    val name: String,
    val registrationDate: LocalDate,
    val address: String,
    val isActive: Boolean
)