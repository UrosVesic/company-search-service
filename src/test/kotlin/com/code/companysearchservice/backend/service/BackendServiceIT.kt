package com.code.companysearchservice.backend.service

import com.code.companysearchservice.BaseDatabaseIntegrationTest
import com.code.companysearchservice.backend.controller.model.BackendServiceResponseDTO
import com.code.companysearchservice.backend.service.model.LookupStatus
import com.code.companysearchservice.verification.repository.VerificationRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.getForEntity
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.Future

class BackendServiceIT(
    @Autowired val verificationRepository: VerificationRepository
) : BaseDatabaseIntegrationTest() {

    @Test
    fun `concurrent requests with same verificationId should not fail`() {
        val verificationId = UUID.randomUUID()
        val threadCount = 5
        val executor = Executors.newFixedThreadPool(threadCount)
        val latch = CountDownLatch(1)

        val futures: List<Future<BackendServiceResponseDTO?>> = (1..threadCount).map {
            executor.submit<BackendServiceResponseDTO?> {
                latch.await()
                val response = testRestTemplate.getForEntity<BackendServiceResponseDTO>(
                    "/backend-service?verificationId={id}&query={q}",
                    verificationId, "CJQUNXGW"
                )
                response.body
            }
        }

        latch.countDown()

        val results = futures.map { it.get() }
        executor.shutdown()

        assertThat(results).allSatisfy { body ->
            assertThat(body).isNotNull
            assertThat(body!!.verificationId).isEqualTo(verificationId)
        }

        val entities = verificationRepository.findAll().filter { it.verificationId == verificationId }
        assertThat(entities).hasSize(1)
    }
}
