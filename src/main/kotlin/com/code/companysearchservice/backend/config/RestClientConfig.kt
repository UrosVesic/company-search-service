package com.code.companysearchservice.backend.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
class RestClientConfig {

    @Bean
    fun thirdPartyRestClient(
        builder: RestClient.Builder,
        @Value("\${app.self-base-url}") baseUrl: String
    ): RestClient = builder.baseUrl(baseUrl).build()
}
