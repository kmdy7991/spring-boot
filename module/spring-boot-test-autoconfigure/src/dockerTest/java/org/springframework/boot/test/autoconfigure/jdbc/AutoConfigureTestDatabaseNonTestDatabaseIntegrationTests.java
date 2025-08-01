/*
 * Copyright 2012-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.test.autoconfigure.jdbc;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabaseNonTestDatabaseIntegrationTests.SetupDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testsupport.container.DisabledIfDockerUnavailable;
import org.springframework.boot.testsupport.container.TestImage;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.TestPropertySourceUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link AutoConfigureTestDatabase} with Docker Compose.
 *
 * @author Phillip Webb
 */
@SpringBootTest
@ContextConfiguration(initializers = SetupDatabase.class)
@AutoConfigureTestDatabase
@OverrideAutoConfiguration(enabled = false)
@DisabledIfDockerUnavailable
class AutoConfigureTestDatabaseNonTestDatabaseIntegrationTests {

	@Container
	static PostgreSQLContainer<?> postgres = TestImage.container(PostgreSQLContainer.class);

	@Autowired
	private DataSource dataSource;

	@Test
	void dataSourceIsReplaced() {
		assertThat(this.dataSource).isInstanceOf(EmbeddedDatabase.class);
	}

	@Configuration
	@ImportAutoConfiguration(DataSourceAutoConfiguration.class)
	static class Config {

	}

	static class SetupDatabase implements ApplicationContextInitializer<ConfigurableApplicationContext> {

		@Override
		public void initialize(ConfigurableApplicationContext applicationContext) {
			postgres.start();
			TestPropertySourceUtils.addInlinedPropertiesToEnvironment(applicationContext,
					"spring.datasource.url=" + postgres.getJdbcUrl());
		}

	}

}
