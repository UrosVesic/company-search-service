package com.code.companysearchservice

import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.jdbc.JdbcTestUtils
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

@ActiveProfiles("test")
@SpringBootTest(
	webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
	properties = ["server.port=18080"]
)
abstract class BaseDatabaseIntegrationTest {

	@Autowired
	lateinit var jdbcTemplate: JdbcTemplate

	@Autowired
	lateinit var testRestTemplate: TestRestTemplate

	@BeforeEach
	fun cleanDatabase() {
		JdbcTestUtils.deleteFromTables(jdbcTemplate, "verifications")
	}

	companion object {
		@JvmStatic
		@ServiceConnection
		val postgresContainer = PostgreSQLContainer(DockerImageName.parse("postgres:latest")).apply {
			start()
		}
	}
}
