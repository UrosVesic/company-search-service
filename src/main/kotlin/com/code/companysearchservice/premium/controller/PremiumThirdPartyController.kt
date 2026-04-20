package com.code.companysearchservice.premium.controller

import com.code.companysearchservice.premium.controller.model.PremiumCompanyDTO
import com.code.companysearchservice.premium.service.PremiumThirdPartyService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class PremiumThirdPartyController(
    private val service: PremiumThirdPartyService
) {

    @GetMapping("/premium-third-party")
    fun search(@RequestParam query: String): List<PremiumCompanyDTO> = service.search(query)
}