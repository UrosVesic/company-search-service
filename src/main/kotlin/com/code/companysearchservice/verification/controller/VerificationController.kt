package com.code.companysearchservice.verification.controller

import com.code.companysearchservice.verification.controller.model.VerificationDTO
import com.code.companysearchservice.verification.service.VerificationService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class VerificationController(private val verificationService: VerificationService) {

    @GetMapping("/verifications/{verificationId}")
    fun getByVerificationId(@PathVariable verificationId: UUID): VerificationDTO =
        verificationService.findById(verificationId)
}