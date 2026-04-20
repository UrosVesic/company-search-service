package com.code.companysearchservice.backend.controller.model

import com.code.companysearchservice.backend.service.model.LookupStatus
import java.util.UUID

data class BackendServiceResponseDTO(
    val verificationId: UUID,
    val query: String,
    val result: ResultDTO,
    val lookupStatus: LookupStatus = LookupStatus.FRESH
)