package com.code.companysearchservice.verification.service

import com.code.companysearchservice.common.exception.HttpStatusException
import com.code.companysearchservice.verification.controller.model.VerificationDTO
import com.code.companysearchservice.verification.mapper.VerificationMapper
import com.code.companysearchservice.verification.repository.VerificationRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class VerificationService(
    private val verificationRepository: VerificationRepository,
    private val verificationMapper: VerificationMapper
) {

    fun findById(verificationId: UUID): VerificationDTO =
        verificationRepository.findByVerificationId(verificationId)
            ?.let(verificationMapper::toVerificationDTO)
            ?: throw HttpStatusException(HttpStatus.NOT_FOUND, "Verification not found")

}