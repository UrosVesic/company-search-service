package com.code.companysearchservice.backend.controller

import com.code.companysearchservice.backend.controller.model.BackendServiceResponseDTO
import com.code.companysearchservice.backend.service.BackendService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class BackendServiceController(private val service: BackendService) {

    @GetMapping("/backend-service")
    fun search(
        @RequestParam verificationId: UUID,
        @RequestParam query: String
    ): BackendServiceResponseDTO = service.search(verificationId, query)

}