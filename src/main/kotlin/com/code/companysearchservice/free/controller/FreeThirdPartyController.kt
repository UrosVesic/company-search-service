package com.code.companysearchservice.free.controller

import com.code.companysearchservice.free.controller.model.FreeCompanyDTO
import com.code.companysearchservice.free.service.FreeThirdPartyService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class FreeThirdPartyController(
    private val service: FreeThirdPartyService
) {
    @GetMapping("/free-third-party")
    fun search(@RequestParam query: String): List<FreeCompanyDTO> =
        service.search(query)
}